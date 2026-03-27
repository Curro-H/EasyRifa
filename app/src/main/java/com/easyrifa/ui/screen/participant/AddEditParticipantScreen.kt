package com.easyrifa.ui.screen.participant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easyrifa.ui.component.NumberGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditParticipantScreen(
    raffleId: Long,
    participantId: Long?,
    onBack: () -> Unit,
    viewModel: AddEditParticipantViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.savedOk) {
        if (uiState.savedOk) onBack()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (participantId == null) "Nuevo participante" else "Editar participante") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nombre") },
                placeholder = { Text("Ej: Juan García") },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Assign mode selector
            Text("Asignación de números", style = MaterialTheme.typography.titleSmall)

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = uiState.assignMode == AssignMode.MANUAL,
                    onClick = { viewModel.onAssignModeChange(AssignMode.MANUAL) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("Manual") }
                SegmentedButton(
                    selected = uiState.assignMode == AssignMode.AUTO,
                    onClick = { viewModel.onAssignModeChange(AssignMode.AUTO) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("Automático") }
            }

            when (uiState.assignMode) {
                AssignMode.MANUAL -> {
                    Text(
                        text = "${uiState.selectedNumbers.size} números seleccionados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    NumberGrid(
                        minNumber = uiState.minNumber,
                        maxNumber = uiState.maxNumber,
                        selectedNumbers = uiState.selectedNumbers,
                        takenNumbers = uiState.takenNumbers,
                        onNumberToggle = viewModel::onNumberToggle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                AssignMode.AUTO -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Números a asignar: ${uiState.autoCount}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.onAutoCountChange(uiState.autoCount - 1) },
                                enabled = uiState.autoCount > 1
                            ) { Text("−") }
                            Text(
                                text = uiState.autoCount.toString(),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Button(
                                onClick = { viewModel.onAutoCountChange(uiState.autoCount + 1) }
                            ) { Text("+") }
                        }
                        Text(
                            text = "Se asignarán ${uiState.autoCount} número(s) aleatorio(s) de los disponibles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
            }

            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) CircularProgressIndicator()
                else Text("Guardar")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
