package com.easyrifa.ui.screen.participant

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyrifa.data.db.entity.AssignedNumberEntity
import com.easyrifa.data.repository.AssignedNumberRepository
import com.easyrifa.data.repository.ParticipantRepository
import com.easyrifa.data.repository.RaffleRepository
import com.easyrifa.domain.usecase.participant.AssignNumbersAutoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AssignMode { MANUAL, AUTO }

data class AddEditParticipantUiState(
    val name: String = "",
    val nameError: String? = null,
    val assignMode: AssignMode = AssignMode.MANUAL,
    val selectedNumbers: Set<Int> = emptySet(),
    val takenNumbers: Set<Int> = emptySet(),
    val autoCount: Int = 1,
    val minNumber: Int = 1,
    val maxNumber: Int = 100,
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedOk: Boolean = false
)

@HiltViewModel
class AddEditParticipantViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val raffleRepository: RaffleRepository,
    private val participantRepository: ParticipantRepository,
    private val assignedNumberRepository: AssignedNumberRepository,
    private val assignNumbersAutoUseCase: AssignNumbersAutoUseCase
) : ViewModel() {

    val raffleId: Long = checkNotNull(savedStateHandle["raffleId"])
    val editParticipantId: Long? = savedStateHandle.get<Long>("participantId")
        ?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(AddEditParticipantUiState())
    val uiState: StateFlow<AddEditParticipantUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Load raffle range
            val raffle = raffleRepository.getRaffleById(raffleId).first()
            val min = raffle?.minNumber ?: 1
            val max = raffle?.maxNumber ?: 100

            // Load all taken numbers (by other participants, not this one)
            val allTaken = assignedNumberRepository.getAssignedNumbersForRaffleOnce(raffleId).toSet()

            if (editParticipantId != null) {
                val participant = participantRepository.getParticipantById(editParticipantId)
                val myNumbers = assignedNumberRepository.getNumbersForParticipantOnce(editParticipantId)
                    .map { it.number }.toSet()

                _uiState.update {
                    it.copy(
                        name = participant?.name ?: "",
                        minNumber = min,
                        maxNumber = max,
                        selectedNumbers = myNumbers,
                        takenNumbers = allTaken - myNumbers // others' numbers (exclude mine)
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        minNumber = min,
                        maxNumber = max,
                        takenNumbers = allTaken
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = null) }
    fun onAssignModeChange(mode: AssignMode) = _uiState.update { it.copy(assignMode = mode) }
    fun onAutoCountChange(count: Int) = _uiState.update { it.copy(autoCount = count.coerceAtLeast(1)) }

    fun onNumberToggle(number: Int) {
        _uiState.update { state ->
            val updated = if (number in state.selectedNumbers)
                state.selectedNumbers - number
            else
                state.selectedNumbers + number
            state.copy(selectedNumbers = updated)
        }
    }

    fun save() = viewModelScope.launch {
        val state = _uiState.value
        val name = state.name.trim()

        if (name.isBlank()) {
            _uiState.update { it.copy(nameError = "El nombre no puede estar vacío") }
            return@launch
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        try {
            val participantId = if (editParticipantId == null) {
                participantRepository.addParticipant(raffleId, name)
            } else {
                val existing = participantRepository.getParticipantById(editParticipantId)!!
                participantRepository.updateParticipant(existing.copy(name = name))
                // Remove all previously assigned numbers to re-assign
                assignedNumberRepository.unassignAllForParticipant(editParticipantId)
                editParticipantId
            }

            when (state.assignMode) {
                AssignMode.MANUAL -> {
                    if (state.selectedNumbers.isNotEmpty()) {
                        assignedNumberRepository.assignMultiple(
                            participantId, raffleId, state.selectedNumbers.toList()
                        ).getOrThrow()
                    }
                }
                AssignMode.AUTO -> {
                    assignNumbersAutoUseCase.execute(
                        participantId = participantId,
                        raffleId = raffleId,
                        minNumber = state.minNumber,
                        maxNumber = state.maxNumber,
                        count = state.autoCount
                    ).getOrThrow()
                }
            }

            _uiState.update { it.copy(isSaving = false, savedOk = true) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isSaving = false, error = e.message) }
        }
    }
}
