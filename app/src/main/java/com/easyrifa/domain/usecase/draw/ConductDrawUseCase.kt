package com.easyrifa.domain.usecase.draw

import androidx.room.withTransaction
import com.easyrifa.data.db.AppDatabase
import com.easyrifa.data.db.entity.DrawnNumberEntity
import com.easyrifa.data.repository.AssignedNumberRepository
import com.easyrifa.data.repository.DrawRepository
import com.easyrifa.data.repository.ParticipantRepository
import javax.inject.Inject

class ConductDrawUseCase @Inject constructor(
    private val assignedNumberRepository: AssignedNumberRepository,
    private val participantRepository: ParticipantRepository,
    private val drawRepository: DrawRepository,
    private val db: AppDatabase
) {
    data class DrawInput(val raffleId: Long, val numberOfWinners: Int)

    data class WinnerResult(
        val number: Int,
        val participantId: Long,
        val participantName: String
    )

    suspend fun execute(input: DrawInput): Result<List<WinnerResult>> = runCatching {
        val allAssigned = assignedNumberRepository.getAllAssignedForRaffle(input.raffleId)

        require(allAssigned.isNotEmpty()) {
            "No hay números asignados para sortear."
        }
        require(input.numberOfWinners <= allAssigned.size) {
            "No puedes sortear más ganadores que números asignados."
        }

        // Load participant names for mapping
        val participants = participantRepository.getParticipantsByRaffleOnce(input.raffleId)
        val participantMap = participants.associateBy { it.id }

        // Shuffle and pick winners
        val winners = allAssigned.shuffled().take(input.numberOfWinners)

        // Persist atomically
        db.withTransaction {
            val drawResultId = drawRepository.saveDrawResult(
                raffleId = input.raffleId,
                numberOfWinners = input.numberOfWinners
            )
            drawRepository.saveDrawnNumbers(
                winners.map { assigned ->
                    DrawnNumberEntity(
                        drawResultId = drawResultId,
                        number = assigned.number,
                        participantId = assigned.participantId,
                        participantName = participantMap[assigned.participantId]?.name ?: ""
                    )
                }
            )
        }

        winners.map { assigned ->
            WinnerResult(
                number = assigned.number,
                participantId = assigned.participantId,
                participantName = participantMap[assigned.participantId]?.name ?: "Desconocido"
            )
        }
    }
}
