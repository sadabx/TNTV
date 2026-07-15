    fun moveFocusVertically(sourceRow: Int, sourceIndex: Int, direction: Int) {
        val targetRow = sourceRow + direction
        val targetRequesters = rowFocusRequesters.getOrNull(targetRow) ?: return
        if (targetRequesters.isEmpty()) return

        val sourceInfo = horizontalListStates[sourceRow].layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == sourceIndex }
        val sourceCenter = sourceInfo?.let { it.offset + it.size / 2 }

        focusScope.launch {
            if (verticalListState.layoutInfo.visibleItemsInfo.none { it.index == targetRow }) {
                verticalListState.scrollToItem(targetRow)
                // Wait for layout to populate visibleItemsInfo
                withFrameNanos { }
                withFrameNanos { }
            }

            val targetState = horizontalListStates[targetRow]
            var targetIndex = sourceIndex.coerceAtMost(targetRequesters.lastIndex)
            
            if (sourceCenter != null && targetState.layoutInfo.visibleItemsInfo.isNotEmpty()) {
                var minDiff = Int.MAX_VALUE
                for (item in targetState.layoutInfo.visibleItemsInfo) {
                    val center = item.offset + item.size / 2
                    val diff = kotlin.math.abs(center - sourceCenter)
                    if (diff < minDiff) {
                        minDiff = diff
                        targetIndex = item.index
                    }
                }
            }

            if (targetState.layoutInfo.visibleItemsInfo.none { it.index == targetIndex }) {
                targetState.scrollToItem(targetIndex)
                withFrameNanos { }
                withFrameNanos { }
            }
            
            // Try requesting focus. If it fails, wait a frame and try again.
            runCatching { targetRequesters[targetIndex].requestFocus() }.onFailure {
                withFrameNanos { }
                runCatching { targetRequesters[targetIndex].requestFocus() }
            }
        }
    }
