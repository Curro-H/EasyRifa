package com.easyrifa.ui.screen.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyrifa.data.db.model.DrawResultWithWinners
import com.easyrifa.data.repository.DrawRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DrawHistoryUiState(
    val draws: List<DrawResultWithWinners> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DrawHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val drawRepository: DrawRepository
) : ViewModel() {

    val raffleId: Long = checkNotNull(savedStateHandle["raffleId"])

    val uiState: StateFlow<DrawHistoryUiState> = drawRepository
        .getDrawResultsWithWinners(raffleId)
        .map { DrawHistoryUiState(draws = it, isLoading = false) }
        .catch { e -> emit(DrawHistoryUiState(isLoading = false, error = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DrawHistoryUiState()
        )

    fun deleteDrawResult(drawResult: com.easyrifa.data.db.entity.DrawResultEntity) =
        viewModelScope.launch {
            drawRepository.deleteDrawResult(drawResult)
        }
}
