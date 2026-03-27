package com.easyrifa.ui.screen.detail

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyrifa.data.db.model.RaffleWithParticipants
import com.easyrifa.data.repository.AssignedNumberRepository
import com.easyrifa.data.repository.ParticipantRepository
import com.easyrifa.data.repository.RaffleRepository
import com.easyrifa.domain.usecase.share.GenerateShareImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RaffleDetailUiState(
    val raffleWithParticipants: RaffleWithParticipants? = null,
    val assignedNumbers: Set<Int> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val shareIntent: Intent? = null
)

@HiltViewModel
class RaffleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val raffleRepository: RaffleRepository,
    private val assignedNumberRepository: AssignedNumberRepository,
    private val participantRepository: ParticipantRepository,
    private val generateShareImageUseCase: GenerateShareImageUseCase
) : ViewModel() {

    val raffleId: Long = checkNotNull(savedStateHandle["raffleId"])

    val uiState: StateFlow<RaffleDetailUiState> = combine(
        raffleRepository.getRaffleWithParticipants(raffleId),
        assignedNumberRepository.getAssignedNumbersForRaffle(raffleId)
    ) { raffleWithParticipants, assignedNumbers ->
        RaffleDetailUiState(
            raffleWithParticipants = raffleWithParticipants,
            assignedNumbers = assignedNumbers.toSet(),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RaffleDetailUiState()
    )

    private val _shareIntent = MutableStateFlow<Intent?>(null)
    val shareIntent: StateFlow<Intent?> = _shareIntent.asStateFlow()

    fun deleteParticipant(participantId: Long) = viewModelScope.launch {
        val participant = participantRepository.getParticipantById(participantId) ?: return@launch
        participantRepository.deleteParticipant(participant)
    }

    fun shareRaffleStatus() = viewModelScope.launch {
        val state = uiState.value
        val raffle = state.raffleWithParticipants?.raffle ?: return@launch

        val shareData = GenerateShareImageUseCase.RaffleShareData(
            raffle = raffle,
            assignedNumbers = state.assignedNumbers,
            totalRange = raffle.maxNumber - raffle.minNumber + 1
        )

        generateShareImageUseCase.execute(shareData).fold(
            onSuccess = { intent -> _shareIntent.value = intent },
            onFailure = { e -> /* emit error if needed */ }
        )
    }

    fun onShareIntentConsumed() {
        _shareIntent.value = null
    }
}
