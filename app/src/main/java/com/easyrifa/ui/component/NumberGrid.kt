package com.easyrifa.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.easyrifa.ui.theme.NumberAssigned
import com.easyrifa.ui.theme.NumberAssignedContent
import com.easyrifa.ui.theme.NumberAvailable
import com.easyrifa.ui.theme.NumberAvailableContent

enum class NumberState { AVAILABLE, SELECTED_MINE, TAKEN_BY_OTHER }

/**
 * Interactive grid of numbers for manual assignment.
 * - AVAILABLE: green, clickable
 * - SELECTED_MINE: orange (primary), clickable (deselect)
 * - TAKEN_BY_OTHER: red container, not clickable
 */
@Composable
fun NumberGrid(
    minNumber: Int,
    maxNumber: Int,
    selectedNumbers: Set<Int>,
    takenNumbers: Set<Int>,
    onNumberToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    val numbers = (minNumber..maxNumber).toList()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 52.dp),
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(numbers, key = { it }) { number ->
            val state = when {
                number in selectedNumbers -> NumberState.SELECTED_MINE
                number in takenNumbers -> NumberState.TAKEN_BY_OTHER
                else -> NumberState.AVAILABLE
            }
            NumberCell(
                number = number,
                state = state,
                onClick = {
                    if (!readOnly && state != NumberState.TAKEN_BY_OTHER) {
                        onNumberToggle(number)
                    }
                }
            )
        }
    }
}

@Composable
fun NumberCell(
    number: Int,
    state: NumberState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            NumberState.AVAILABLE -> NumberAvailable
            NumberState.SELECTED_MINE -> NumberAssigned
            NumberState.TAKEN_BY_OTHER -> MaterialTheme.colorScheme.errorContainer
        },
        label = "NumberCellBg"
    )
    val textColor by animateColorAsState(
        targetValue = when (state) {
            NumberState.AVAILABLE -> NumberAvailableContent
            NumberState.SELECTED_MINE -> NumberAssignedContent
            NumberState.TAKEN_BY_OTHER -> MaterialTheme.colorScheme.onErrorContainer
        },
        label = "NumberCellText"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .clickable(enabled = state != NumberState.TAKEN_BY_OTHER, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

/**
 * Read-only version of NumberGrid for the "Numbers" tab in raffle detail
 * and for the share image preview. Shows assigned vs. free.
 */
@Composable
fun ReadOnlyNumberGrid(
    minNumber: Int,
    maxNumber: Int,
    assignedNumbers: Set<Int>,
    modifier: Modifier = Modifier
) {
    NumberGrid(
        minNumber = minNumber,
        maxNumber = maxNumber,
        selectedNumbers = assignedNumbers,
        takenNumbers = emptySet(),
        onNumberToggle = {},
        modifier = modifier,
        readOnly = true
    )
}
