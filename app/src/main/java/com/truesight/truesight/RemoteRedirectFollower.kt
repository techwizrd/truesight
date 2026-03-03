package com.truesight.truesight

import android.util.Log
import com.truesight.truesight.shared.RemoteRedirectResolver
import com.truesight.truesight.shared.RedirectFollower
import java.net.HttpURLConnection
import java.net.URL
import java.io.InputStream
import java.util.LinkedHashMap
import java.util.concurrent.TimeUnit

object RemoteRedirectFollower : RedirectFollower {
    private const val tag = "RedirectFollower"
    private const val timeoutMs = 3500
    private const val userAgent = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.7632.46 Mobile Safari/537.36"
    private const val cacheCapacity = 128
    private const val cleanupInterval = 16
    private const val maxBodyChars = 256_000
    private val queryParamValuePattern = Regex("([?&][^=&#\\s]+)=([^&#\\s]*)")
    private val verboseUrlLoggingEnabled: Boolean by lazy { resolveDebugBuildFlag() }
    private val successTtlMs = TimeUnit.MINUTES.toMillis(10)
    private val unchangedTtlMs = TimeUnit.MINUTES.toMillis(1)
    private val cacheLock = Any()
    private var writeCount = 0
    private val redirectCache = object : LinkedHashMap<CacheKey, CacheEntry>(cacheCapacity, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<CacheKey, CacheEntry>?): Boolean {
            return size > cacheCapacity
        }
    }

    fun followIfNeeded(url: String): String {
        return followIfNeeded(url, CleanerPolicy())
    }

    override fun follow(url: String, policy: CleanerPolicy): String {
        return followIfNeeded(url, policy)
    }

    fun followIfNeeded(url: String, policy: CleanerPolicy): String {
        val now = System.currentTimeMillis()
        val cacheKey = CacheKey(url = url, policy = policy)
        synchronized(cacheLock) {
            val cached = redirectCache[cacheKey]
            if (cached != null) {
                if (cached.expiresAtMs > now) {
                    debugLog("Cache hit for url=$url")
                    return cached.value
                }

                redirectCache.remove(cacheKey)
                debugLog("Cache expired for url=$url")
            }
        }

        val resolved = RemoteRedirectResolver.followIfNeeded(
            url = url,
            policy = policy,
            networkClient = RemoteRedirectResolver.RedirectNetworkClient { requestUrl ->
                fetchRedirectLocation(requestUrl)
            },
            bodyFetcher = RemoteRedirectResolver.BodyFetcher { requestUrl ->
                fetchBody(requestUrl)
            },
            logger = { level, message, error ->
                when (level) {
                    RemoteRedirectResolver.LogLevel.DEBUG -> debugLog(message)
                    RemoteRedirectResolver.LogLevel.WARN -> {
                        warnLog(message, error)
                    }
                }
            }
        )

        val ttlMs = if (resolved == url) unchangedTtlMs else successTtlMs
        val cacheTime = System.currentTimeMillis()
        synchronized(cacheLock) {
            writeCount += 1
            redirectCache[cacheKey] = CacheEntry(
                value = resolved,
                expiresAtMs = cacheTime + ttlMs
            )
            if (writeCount % cleanupInterval == 0) {
                cleanupExpiredEntries(cacheTime)
            }
        }

        return resolved
    }

    fun invalidateCache() {
        synchronized(cacheLock) {
            redirectCache.clear()
            writeCount = 0
        }
    }

    private fun cleanupExpiredEntries(now: Long) {
        val iterator = redirectCache.entries.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().value.expiresAtMs <= now) {
                iterator.remove()
            }
        }
    }

    private data class CacheEntry(
        val value: String,
        val expiresAtMs: Long
    )

    private data class CacheKey(
        val url: String,
        // TODO: Narrow cache key to redirect-relevant policy flags to improve hit rate.
        val policy: CleanerPolicy
    )

    private fun fetchRedirectLocation(url: String): String? {
        val connection = try {
            (URL(url).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                requestMethod = "GET"
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                setRequestProperty("User-Agent", userAgent)
            }
        } catch (e: Exception) {
            warnLog("Failed opening connection for url=$url", e)
            return null
        }

        return try {
            val responseCode = connection.responseCode
            if (responseCode !in 300..399) {
                debugLog("No redirect for url=$url code=$responseCode")
                null
            } else {
                val location = connection.getHeaderField("Location")
                debugLog("Redirect response for url=$url code=$responseCode location=$location")
                location
            }
        } catch (e: Exception) {
            warnLog("Failed reading redirect response for url=$url", e)
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun fetchBody(url: String): String? {
        val connection = try {
            (URL(url).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                requestMethod = "GET"
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                setRequestProperty("User-Agent", userAgent)
            }
        } catch (e: Exception) {
            warnLog("Failed opening body connection for url=$url", e)
            return null
        }

        return try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                debugLog("Body request failed code=$responseCode url=$url")
                null
            } else {
                debugLog("Body request success code=$responseCode url=$url")
                connection.inputStream.use { readBodyWithLimit(it) }
            }
        } catch (e: Exception) {
            warnLog("Failed fetching body for url=$url", e)
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun debugLog(message: String) {
        if (!verboseUrlLoggingEnabled) {
            return
        }
        Log.d(tag, redactQueryValues(message))
    }

    private fun warnLog(message: String, error: Throwable? = null) {
        val safeMessage = redactQueryValues(message)
        if (error != null) {
            Log.w(tag, safeMessage, error)
        } else {
            Log.w(tag, safeMessage)
        }
    }

    private fun redactQueryValues(message: String): String {
        return queryParamValuePattern.replace(message) { match ->
            "${match.groupValues[1]}=<redacted>"
        }
    }

    private fun resolveDebugBuildFlag(): Boolean {
        return try {
            Class.forName("com.truesight.truesight.BuildConfig")
                .getField("DEBUG")
                .getBoolean(null)
        } catch (_: Exception) {
            false
        }
    }

    internal fun readBodyWithLimit(inputStream: InputStream, maxChars: Int = maxBodyChars): String {
        require(maxChars > 0) { "maxChars must be greater than zero" }

        return inputStream.bufferedReader().use { reader ->
            val content = StringBuilder(minOf(4096, maxChars))
            val buffer = CharArray(2048)

            while (content.length < maxChars) {
                val remaining = maxChars - content.length
                val charsRead = reader.read(buffer, 0, minOf(buffer.size, remaining))
                if (charsRead == -1) {
                    break
                }
                content.append(buffer, 0, charsRead)
            }

            content.toString()
        }
    }
}
