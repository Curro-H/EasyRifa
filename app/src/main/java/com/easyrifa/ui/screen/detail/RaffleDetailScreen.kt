package com.easyrifa.ui.screen.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.easyrifa.ui.component.ParticipantCard
import com.easyrifa.ui.component.ReadOnlyNumberGrid
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaffleDetailScreen(
    raffleId: Long,
    onBack: () -> Unit,
    onEditRaffle: () -> Unit,
    onAddParticipant: () -> Unit,
    onEditParticipant: (Long) -> Unit,
    onStartDraw: () -> Unit,
    onHistory: () -> Unit,
    viewModel: RaffleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shareIntent by viewModel.shareIntent.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var participantToDelete by remember { mutableLongStateOf(-1L) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Launch share chooser when intent is ready
    LaunchedEffect(shareIntent) {
        shareIntent?.let { intent ->
            context.startActivity(android.content.Intent.createChooser(intent, "Compartir via…"))
            viewModel.onShareIntentConsumed()
        }
    }

    val raffle = uiState.raffleWithParticipants?.raffle
    val participants = uiState.raffleWithParticipants?.participants ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(raffle?.name ?: "Sorteo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::shareRaffleStatus) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir estado")
                    }
                    IconButton(onClick = onHistory) {
                        Icon(Icons.Default.History, contentDescription = "Historial")
                    }
                    IconButton(onClick = onEditRaffle) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar sorteo")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExtendedFloatingActionButton(
                        onClick = onStartDraw,
                        icon = { Icon(Icons.Default.Star, contentDescription = null) },
                        text = { Text("Sortear") }
                    )
                    ExtendedFloatingActionButton(
                        onClick = onAddParticipant,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Participante") },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    ) { paddingValues ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Prize image header
            raffle?.imagePath?.let { path ->
                AsyncImage(
                    model = File(path),
                    contentDescription = "Foto del premio",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(MaterialTheme.shapes.medium)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Range + stats
            raffle?.let {
                Text(
                    text = "Rango: ${it.minNumber}–${it.maxNumber}  ·  " +
                            "${uiState.assignedNumbers.size} ocupados · " +
                            "${it.maxNumber - it.minNumber + 1 - uiState.assignedNumbers.size} libres",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Tabs
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Participantes") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Números") }
                )
            }

            when (selectedTab) {
                0 -> {
                    if (participants.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay participantes.\nPulsa + para añadir uno.",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(participants, key = { it.participant.id }) { participantWithNumbers ->
                                ParticipantCard(
                                    participantWithNumbers = participantWithNumbers,
                                    onEdit = { onEditParticipant(participantWithNumbers.participant.id) },
                                    onDelete = {
                                        participantToDelete = participantWithNumbers.participant.id
                                        showDeleteDialog = true
                                    }
                                )
                            }
                            item { Spacer(Modifier.height(80.dp)) } // FAB space
                        }
                    }
                }

                1 -> {
                    raffle?.let {
                        ReadOnlyNumberGrid(
                            minNumber = it.minNumber,
                            maxNumber = it.maxNumber,
                            assignedNumbers = uiState.assignedNumbers,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar participante") },
            text = { Text("¿Eliminar este participante? Se liberarán sus números asignados.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteParticipant(participantToDelete)
                    showDeleteDialog = false
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
