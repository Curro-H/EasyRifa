package com.easyrifa.ui.screen.draw

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easyrifa.ui.component.AnimatedDrawNumber
import com.easyrifa.ui.component.WinnerCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DrawScreen(
    raffleId: Long,
    onBack: () -> Unit,
    viewModel: DrawViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val phase by viewModel.phase.collectAsStateWithLifecycle()
    val numberOfWinners by viewModel.numberOfWinners.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sorteo: ${uiState.raffleName}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentPhase = phase) {
                DrawPhase.Idle -> IdlePhase(
                    assignedCount = uiState.assignedNumberCount,
                    numberOfWinners = numberOfWinners,
                    maxWinners = uiState.assignedNumberCount.coerceAtLeast(1),
                    onNumberOfWinnersChange = { viewModel.setNumberOfWinners(it) },
                    onStartDraw = { viewModel.startDraw() }
                )

                DrawPhase.Animating -> AnimatingPhase(
                    numberOfWinners = numberOfWinners,
                    minNumber = uiState.minNumber,
                    maxNumber = uiState.maxNumber
                )

                is DrawPhase.Results -> ResultsPhase(
                    winners = currentPhase.winners,
                    onDrawAgain = { viewModel.resetDraw() },
                    onBack = onBack
                )
            }
        }
    }
}

@Composable
private fun IdlePhase(
    assignedCount: Int,
    numberOfWinners: Int,
    maxWinners: Int,
    onNumberOfWinnersChange: (Int) -> Unit,
    onStartDraw: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎟️",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "$assignedCount números asignados",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))

        if (assignedCount == 0) {
            Text(
                text = "No hay números asignados para sortear.\nAñade participantes primero.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(
                text = "Número de ganadores: $numberOfWinners",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = numberOfWinners.toFloat(),
                onValueChange = { onNumberOfWinnersChange(it.toInt()) },
                valueRange = 1f..maxWinners.coerceAtLeast(1).toFloat(),
                steps = (maxWinners - 1).coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onStartDraw,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("¡Sortear!", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnimatingPhase(
    numberOfWinners: Int,
    minNumber: Int,
    maxNumber: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sorteando…",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(32.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(numberOfWinners) {
                AnimatedDrawNumber(
                    targetNumber = null, // still spinning
                    minNumber = minNumber,
                    maxNumber = maxNumber
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        CircularProgressIndicator()
    }
}

@Composable
private fun ResultsPhase(
    winners: List<com.easyrifa.ui.component.WinnerDisplay>,
    onDrawAgain: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "🏆 Resultado del sorteo",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        itemsIndexed(winners) { index, winner ->
            WinnerCard(winner = winner, position = index)
        }

        item {
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDrawAgain,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Sortear de nuevo") }
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Volver") }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
