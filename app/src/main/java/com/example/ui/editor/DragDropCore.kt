package com.example.ui.editor

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Core engine state for touch-based drag-and-drop reordering within a LazyColumn.
 */
class DragDropState internal constructor(
    val lazyListState: LazyListState,
    private val scope: CoroutineScope,
    private val headerItemCount: Int,
    private val onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    private val onDragEnd: () -> Unit,
    private val hapticFeedback: HapticFeedback
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    var draggingItemOffset by mutableFloatStateOf(0f)
        private set

    private var initialItemTop by mutableIntStateOf(0)
    private var initialItemSize by mutableIntStateOf(0)

    private val scrollChannel = Channel<Float>(Channel.CONFLATED)

    init {
        scope.launch {
            while (true) {
                val scrollDelta = scrollChannel.receive()
                lazyListState.scrollBy(scrollDelta)
            }
        }
    }

    val isDragging: Boolean
        get() = draggingItemIndex != null

    fun onDragStart(pointerOffset: Offset) {
        val y = pointerOffset.y
        val item = lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.offset <= y && (it.offset + it.size) >= y }

        if (item != null) {
            val listIndex = item.index - headerItemCount
            if (listIndex >= 0) {
                draggingItemIndex = listIndex
                initialItemTop = item.offset
                initialItemSize = item.size
                draggingItemOffset = 0f
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    fun onHandleDragStart(targetBlockIndex: Int) {
        val item = lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == targetBlockIndex + headerItemCount }

        draggingItemIndex = targetBlockIndex
        if (item != null) {
            initialItemTop = item.offset
            initialItemSize = item.size
        } else {
            initialItemTop = 0
            initialItemSize = 100
        }
        draggingItemOffset = 0f
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun onDrag(dragAmountY: Float) {
        val currentIndex = draggingItemIndex ?: return
        draggingItemOffset += dragAmountY

        // Check for viewport edge auto-scroll
        val currentVisibleItem = lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == currentIndex + headerItemCount }

        if (currentVisibleItem != null) {
            val currentItemCenter = currentVisibleItem.offset + (currentVisibleItem.size / 2) + draggingItemOffset
            val viewportStart = lazyListState.layoutInfo.viewportStartOffset
            val viewportEnd = lazyListState.layoutInfo.viewportEndOffset
            val edgeThreshold = 100f

            val distanceFromTop = currentItemCenter - viewportStart
            val distanceFromBottom = viewportEnd - currentItemCenter

            if (distanceFromTop < edgeThreshold) {
                val speed = -((edgeThreshold - distanceFromTop) / edgeThreshold) * 25f
                scrollChannel.trySend(speed)
            } else if (distanceFromBottom < edgeThreshold) {
                val speed = ((edgeThreshold - distanceFromBottom) / edgeThreshold) * 25f
                scrollChannel.trySend(speed)
            }
        }

        // Detect item displacement and trigger live reordering
        checkAndPerformSwap()
    }

    private fun checkAndPerformSwap() {
        val currentIdx = draggingItemIndex ?: return
        val currentLayoutItem = lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == currentIdx + headerItemCount } ?: return

        val draggedCenter = currentLayoutItem.offset + (currentLayoutItem.size / 2f) + draggingItemOffset

        // Check neighboring items
        for (item in lazyListState.layoutInfo.visibleItemsInfo) {
            val otherIdx = item.index - headerItemCount
            if (otherIdx < 0 || otherIdx == currentIdx) continue

            val otherCenter = item.offset + (item.size / 2f)

            // If we moved down past other item's center
            if (otherIdx > currentIdx && draggedCenter > otherCenter) {
                onMove(currentIdx, otherIdx)
                draggingItemOffset -= item.size
                draggingItemIndex = otherIdx
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                break
            }

            // If we moved up past other item's center
            if (otherIdx < currentIdx && draggedCenter < otherCenter) {
                onMove(currentIdx, otherIdx)
                draggingItemOffset += item.size
                draggingItemIndex = otherIdx
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                break
            }
        }
    }

    fun onDragStopped() {
        if (draggingItemIndex != null) {
            draggingItemIndex = null
            draggingItemOffset = 0f
            onDragEnd()
        }
    }

    fun onDragCancelled() {
        if (draggingItemIndex != null) {
            draggingItemIndex = null
            draggingItemOffset = 0f
            onDragEnd()
        }
    }
}

/**
 * Remember and create a DragDropState tied to the lazy list.
 */
@Composable
fun rememberDragDropState(
    lazyListState: LazyListState,
    headerItemCount: Int = 2,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    onDragEnd: () -> Unit = {}
): DragDropState {
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val currentOnMove by rememberUpdatedState(onMove)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    return remember(lazyListState) {
        DragDropState(
            lazyListState = lazyListState,
            scope = scope,
            headerItemCount = headerItemCount,
            onMove = { from, to -> currentOnMove(from, to) },
            onDragEnd = { currentOnDragEnd() },
            hapticFeedback = hapticFeedback
        )
    }
}

/**
 * Container modifier that enables long-press dragging across items in the LazyColumn.
 */
fun Modifier.dragContainer(
    dragDropState: DragDropState,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this

    pointerInput(dragDropState) {
        detectDragGesturesAfterLongPress(
            onDragStart = { offset ->
                dragDropState.onDragStart(offset)
            },
            onDrag = { change, dragAmount ->
                change.consume()
                dragDropState.onDrag(dragAmount.y)
            },
            onDragEnd = {
                dragDropState.onDragStopped()
            },
            onDragCancel = {
                dragDropState.onDragCancelled()
            }
        )
    }
}

/**
 * Modifier for dedicated drag handles that allows immediate drag without waiting for long press.
 */
fun Modifier.dragHandleGesture(
    dragDropState: DragDropState,
    itemIndex: Int,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this

    pointerInput(itemIndex, dragDropState) {
        detectDragGestures(
            onDragStart = {
                dragDropState.onHandleDragStart(itemIndex)
            },
            onDrag = { change, dragAmount ->
                change.consume()
                dragDropState.onDrag(dragAmount.y)
            },
            onDragEnd = {
                dragDropState.onDragStopped()
            },
            onDragCancel = {
                dragDropState.onDragCancelled()
            }
        )
    }
}

/**
 * Item modifier that applies visual elevation, scale, and translation to the item being dragged.
 */
fun Modifier.draggableItemLayer(
    dragDropState: DragDropState,
    itemIndex: Int
): Modifier = composed {
    val isDragging = dragDropState.draggingItemIndex == itemIndex

    this
        .zIndex(if (isDragging) 15f else 1f)
        .graphicsLayer {
            translationY = if (isDragging) dragDropState.draggingItemOffset else 0f
            scaleX = if (isDragging) 1.025f else 1f
            scaleY = if (isDragging) 1.025f else 1f
            shadowElevation = if (isDragging) 24f else 0f
        }
}
