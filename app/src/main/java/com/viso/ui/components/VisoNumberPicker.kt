package com.viso.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.TextMuted
import com.viso.ui.theme.TextPrimary
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VisoNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    label: String,
    modifier: Modifier = Modifier,
    displayTransform: (Int) -> String = { it.toString() }
) {
    val items = range.toList()
    val itemHeight = 40.dp
    val initialIndex = (items.indexOf(value)).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val highlightColor = AccentBlue.copy(alpha = 0.15f)
    val borderColor = AccentBlue.copy(alpha = 0.3f)

    LaunchedEffect(value) {
        val targetIndex = items.indexOf(value)
        if (targetIndex >= 0 && targetIndex != listState.firstVisibleItemIndex) {
            listState.scrollToItem(targetIndex)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            if (!listState.isScrollInProgress) {
                val centerIndex = listState.firstVisibleItemIndex
                centerIndex.coerceIn(0, items.size - 1)
            } else null
        }
            .distinctUntilChanged()
            .collect { index ->
                if (index != null && index in items.indices) {
                    onValueChange(items[index])
                }
            }
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        modifier = Modifier.padding(bottom = 4.dp)
    )

    LazyColumn(
        state = listState,
        flingBehavior = snapBehavior,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp, max = 140.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top padding item
        item {
            Box(modifier = Modifier.height(itemHeight))
        }

        items(items.size) { index ->
            val isCenter = listState.firstVisibleItemIndex == index

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .then(
                        if (isCenter) {
                            Modifier
                                .background(highlightColor)
                                .drawBehind {
                                    val strokeWidth = 1.dp.toPx()
                                    drawLine(
                                        color = borderColor,
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, 0f),
                                        strokeWidth = strokeWidth
                                    )
                                    drawLine(
                                        color = borderColor,
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = strokeWidth
                                    )
                                }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayTransform(items[index]),
                    fontSize = if (isCenter) 18.sp else 14.sp,
                    fontWeight = if (isCenter) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isCenter) TextPrimary else TextMuted.copy(alpha = 0.3f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Bottom padding item
        item {
            Box(modifier = Modifier.height(itemHeight))
        }
    }
}
