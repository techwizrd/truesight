package com.truesight.truesight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val URL_BOX_BACKGROUND_ALPHA = 0.35f
private const val SCROLL_TRACK_ALPHA = 0.2f
private const val SCROLL_THUMB_ALPHA = 0.6f
private const val MIN_THUMB_FRACTION = 0.2f

@Composable
internal fun ScrollableUrlText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    maxVisibleHeight: Dp = 96.dp
) {
    val scrollState = rememberScrollState()
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = SCROLL_TRACK_ALPHA)
    val thumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = SCROLL_THUMB_ALPHA)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = maxVisibleHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = URL_BOX_BACKGROUND_ALPHA))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = style,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        )

        if (scrollState.maxValue > 0) {
            val viewportPx = constraints.maxHeight.toFloat()
            val contentPx = viewportPx + scrollState.maxValue.toFloat()
            val thumbFraction = (viewportPx / contentPx).coerceIn(MIN_THUMB_FRACTION, 1f)
            val thumbHeight = maxHeight * thumbFraction
            val progress = scrollState.value.toFloat() / scrollState.maxValue.toFloat()
            val thumbOffset = (maxHeight - thumbHeight) * progress

            VerticalScrollIndicator(
                trackColor = trackColor,
                thumbColor = thumbColor,
                thumbHeight = thumbHeight,
                thumbOffset = thumbOffset
            )
        }
    }
}

@Composable
private fun BoxScope.VerticalScrollIndicator(
    trackColor: Color,
    thumbColor: Color,
    thumbHeight: Dp,
    thumbOffset: Dp
) {
    Box(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
            .width(3.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(trackColor)
    )
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(y = thumbOffset)
            .height(thumbHeight)
            .width(3.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(thumbColor)
    )
}
