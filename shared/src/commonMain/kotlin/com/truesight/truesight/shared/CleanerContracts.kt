package com.truesight.truesight.shared

interface CleanerPolicyStore {
    fun loadPolicy(): CleanerPolicy
    fun savePolicy(policy: CleanerPolicy)
}

fun interface RedirectFollower {
    fun follow(url: String, policy: CleanerPolicy): String
}
