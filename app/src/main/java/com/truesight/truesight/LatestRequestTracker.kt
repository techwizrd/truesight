package com.truesight.truesight

internal class LatestRequestTracker {
    private var latestRequestId: Long = 0

    fun nextRequestId(): Long {
        latestRequestId += 1
        return latestRequestId
    }

    fun isLatest(requestId: Long): Boolean {
        return requestId == latestRequestId
    }
}
