package com.easyrifa.domain.usecase.participant

import com.easyrifa.data.repository.AssignedNumberRepository
import javax.inject.Inject

class AssignNumbersAutoUseCase @Inject constructor(
    private val assignedNumberRepository: AssignedNumberRepository
) {
    /**
     * Automatically assigns [count] random available numbers to [participantId].
     * Only picks from numbers not yet assigned to anyone in the raffle.
     */
    suspend fun execute(
        participantId: Long,
        raffleId: Long,
        minNumber: Int,
        maxNumber: Int,
        count: Int
    ): Result<List<Int>> = runCatching {
        val alreadyAssigned = assignedNumberRepository.getAssignedNumbersForRaffleOnce(raffleId).toSet()
        val available = (minNumber..maxNumber).filter { it !in alreadyAssigned }.shuffled()

        require(available.size >= count) {
            "No hay suficientes números disponibles. Disponibles: ${available.size}, solicitados: $count"
        }

        val chosen = available.take(count)
        assignedNumberRepository.assignMultiple(participantId, raffleId, chosen).getOrThrow()
        chosen
    }
}
