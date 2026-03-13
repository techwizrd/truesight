package com.truesight.truesight

internal interface PolicyVersionProvider {
    fun currentVersion(): Long
}
