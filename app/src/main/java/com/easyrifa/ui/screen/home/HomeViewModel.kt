package com.easyrifa.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyrifa.data.db.entity.RaffleEntity
import com.easyrifa.data.repository.AssignedNumberRepository
import com.easyrifa.data.repository.ParticipantRepository
import com.easyrifa.data.repository.RaffleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RaffleSummary(
    val raffle: RaffleEntity,
    val participantCount: Int
)

data class HomeUiState(
    val raffles: List<RaffleSummary> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val raffleRepository: RaffleRepository,
    private val participantRepository: ParticipantRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = raffleRepository.getAllRaffles()
        .map { raffles ->
            val summaries = raffles.map { raffle ->
                RaffleSummary(
                    raffle = raffle,
                    participantCount = participantRepository.getParticipantsByRaffleOnce(raffle.id).size
                )
            }
            HomeUiState(raffles = summaries, isLoading = false)
        }
        .catch { e -> emit(HomeUiState(isLoading = false, error = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    fun deleteRaffle(raffle: RaffleEntity) = viewModelScope.launch {
        raffleRepository.deleteRaffle(raffle)
    }
}
