package com.truesight.truesight

object RedirectUnwrapper {
    fun unwrap(url: String): String {
        return com.truesight.truesight.shared.RedirectUnwrapper.unwrap(url)
    }

    fun unwrap(url: String, policy: CleanerPolicy): String {
        return com.truesight.truesight.shared.RedirectUnwrapper.unwrap(url, policy)
    }
}
