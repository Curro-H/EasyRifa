package com.easyrifa.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Shows a spinning number animation while [targetNumber] is null,
 * then decelerates and reveals the result when [targetNumber] is set.
 */
@Composable
fun AnimatedDrawNumber(
    targetNumber: Int?,
    minNumber: Int,
    maxNumber: Int,
    modifier: Modifier = Modifier
) {
    var displayedNumber by remember { mutableIntStateOf(minNumber) }
    var revealed by remember { mutableStateOf(false) }

    LaunchedEffect(targetNumber) {
        if (targetNumber == null) {
            revealed = false
            // Spin rapidly while no result
            while (true) {
                displayedNumber = (minNumber..maxNumber).random()
                delay(80)
            }
        } else {
            // Deceleration effect: several more ticks getting slower
            val decelerationDelays = listOf(70L, 100L, 140L, 190L, 250L, 320L, 400L)
            for (d in decelerationDelays) {
                displayedNumber = (minNumber..maxNumber).random()
                delay(d)
            }
            displayedNumber = targetNumber
            revealed = true
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (revealed) 1.25f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "RevealScale",
        finishedListener = { /* bounce done */ }
    )

    Box(
        modifier = modifier
            .size(88.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayedNumber.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
