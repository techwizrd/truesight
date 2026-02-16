package com.truesight.truesight.shared

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

internal data class RedditPostData(
    val isSelf: Boolean,
    val postHint: String?,
    val destinationUrl: String?
)

internal object RedditPostDataParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseFirstPostData(payload: String): RedditPostData? {
        val root = runCatching { json.parseToJsonElement(payload) }.getOrNull() ?: return null
        val postData = findFirstPostDataObject(root) ?: return null

        return RedditPostData(
            isSelf = postData.boolean("is_self") == true,
            postHint = postData.string("post_hint"),
            destinationUrl = postData.string("url_overridden_by_dest") ?: postData.string("url")
        )
    }

    private fun findFirstPostDataObject(root: JsonElement): JsonObject? {
        val listingObject = when (root) {
            is JsonArray -> root.firstOrNull()
            else -> root
        } as? JsonObject ?: return null

        val listingData = listingObject.objectAt("data") ?: return null
        val children = listingData["children"] as? JsonArray ?: return null
        val firstChild = children.firstOrNull() as? JsonObject ?: return null

        return firstChild.objectAt("data")
    }

    private fun JsonObject.objectAt(key: String): JsonObject? {
        return this[key]?.jsonObject
    }

    private fun JsonObject.string(key: String): String? {
        return (this[key] as? JsonPrimitive)?.contentOrNull
    }

    private fun JsonObject.boolean(key: String): Boolean? {
        return (this[key] as? JsonPrimitive)?.booleanOrNull
    }
}
