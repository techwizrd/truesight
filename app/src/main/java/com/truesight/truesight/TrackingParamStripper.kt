package com.truesight.truesight

object TrackingParamStripper {
    fun strip(url: String): String {
        return com.truesight.truesight.shared.TrackingParamStripper.strip(url)
    }

    fun strip(url: String, policy: CleanerPolicy): String {
        return com.truesight.truesight.shared.TrackingParamStripper.strip(url, policy)
    }
}
