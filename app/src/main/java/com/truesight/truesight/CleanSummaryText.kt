package com.truesight.truesight

import android.content.Context

internal fun cleanedSummaryText(
    context: Context,
    paramsRemoved: Int,
    redirectsFollowed: Int
): String {
    val paramsPart = context.resources.getQuantityString(
        R.plurals.params_removed_count,
        paramsRemoved,
        paramsRemoved
    )
    val redirectsPart = context.resources.getQuantityString(
        R.plurals.redirects_followed_count,
        redirectsFollowed,
        redirectsFollowed
    )

    return context.getString(
        R.string.cleaned_summary_template,
        paramsPart,
        redirectsPart
    )
}
