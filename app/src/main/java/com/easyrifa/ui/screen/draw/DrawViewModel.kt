package com.easyrifa.ui.screen.draw

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyrifa.data.repository.AssignedNumberRepository
import com.easyrifa.data.repository.RaffleRepository
import com.easyrifa.domain.usecase.draw.ConductDrawUseCase
import com.easyrifa.ui.component.WinnerDisplay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DrawPhase {
    object Idle : DrawPhase()
    object Animating : DrawPhase()
    data class Results(val winners: List<WinnerDisplay>) : DrawPhase()
}

data class DrawUiState(
    val raffleName: String = "",
    val minNumber: Int = 1,
    val maxNumber: Int = 100,
    val assignedNumberCount: Int = 0,
    val numberOfWinnersInput: Int = 1,
    val phase: DrawPhase = DrawPhase.Idle,
    val error: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class DrawViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val raffleRepository: RaffleRepository,
    private val assignedNumberRepository: AssignedNumberRepository,
    private val conductDrawUseCase: ConductDrawUseCase
) : ViewModel() {

    val raffleId: Long = checkNotNull(savedStateHandle["raffleId"])

    val uiState: StateFlow<DrawUiState> = combine(
        raffleRepository.getRaffleById(raffleId),
        assignedNumberRepository.getAssignedCountForRaffle(raffleId)
    ) { raffle, assignedCount ->
        DrawUiState(
            raffleName = raffle?.name ?: "",
            minNumber = raffle?.minNumber ?: 1,
            maxNumber = raffle?.maxNumber ?: 100,
            assignedNumberCount = assignedCount,
            numberOfWinnersInput = _numberOfWinners.value,
            phase = _phase.value,
            error = _error.value,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DrawUiState()
    )

    private val _numberOfWinners = MutableStateFlow(1)
    private val _phase = MutableStateFlow<DrawPhase>(DrawPhase.Idle)
    private val _error = MutableStateFlow<String?>(null)

    // Separate state for UI components that need to observe independently
    val phase: StateFlow<DrawPhase> = _phase.asStateFlow()
    val numberOfWinners: StateFlow<Int> = _numberOfWinners.asStateFlow()

    fun setNumberOfWinners(n: Int) {
        _numberOfWinners.update { n.coerceAtLeast(1) }
    }

    fun startDraw() = viewModelScope.launch {
        if (_phase.value != DrawPhase.Idle) return@launch

        _error.value = null
        _phase.value = DrawPhase.Animating

        // Run the draw computation concurrently with the animation
        val drawDeferred = async {
            conductDrawUseCase.execute(
                ConductDrawUseCase.DrawInput(raffleId, _numberOfWinners.value)
            )
        }

        // Let animation spin for at least 2.5 seconds
        delay(2_500)
        val result = drawDeferred.await()

        result.fold(
            onSuccess = { winners ->
                _phase.value = DrawPhase.Results(
                    winners.map { WinnerDisplay(it.number, it.participantName) }
                )
            },
            onFailure = { e ->
                _phase.value = DrawPhase.Idle
                _error.value = e.message
            }
        )
    }

    fun resetDraw() {
        _phase.value = DrawPhase.Idle
        _error.value = null
    }
}
