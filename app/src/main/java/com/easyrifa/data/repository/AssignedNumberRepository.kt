package com.easyrifa.data.repository

import com.easyrifa.data.db.dao.AssignedNumberDao
import com.easyrifa.data.db.entity.AssignedNumberEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface AssignedNumberRepository {
    fun getAssignedNumbersForRaffle(raffleId: Long): Flow<List<Int>>
    fun getAssignedCountForRaffle(raffleId: Long): Flow<Int>
    suspend fun getAssignedNumbersForRaffleOnce(raffleId: Long): List<Int>
    suspend fun getAllAssignedForRaffle(raffleId: Long): List<AssignedNumberEntity>
    fun getNumbersForParticipant(participantId: Long): Flow<List<AssignedNumberEntity>>
    suspend fun getNumbersForParticipantOnce(participantId: Long): List<AssignedNumberEntity>
    suspend fun isNumberTaken(raffleId: Long, number: Int): Boolean
    suspend fun assignNumber(participantId: Long, raffleId: Long, number: Int): Result<Unit>
    suspend fun assignMultiple(participantId: Long, raffleId: Long, numbers: List<Int>): Result<Unit>
    suspend fun unassignNumber(entity: AssignedNumberEntity)
    suspend fun unassignAllForParticipant(participantId: Long)
}

@Singleton
class AssignedNumberRepositoryImpl @Inject constructor(
    private val assignedNumberDao: AssignedNumberDao
) : AssignedNumberRepository {

    override fun getAssignedNumbersForRaffle(raffleId: Long): Flow<List<Int>> =
        assignedNumberDao.getAssignedNumbersForRaffle(raffleId)

    override fun getAssignedCountForRaffle(raffleId: Long): Flow<Int> =
        assignedNumberDao.getAssignedCountForRaffle(raffleId)

    override suspend fun getAssignedNumbersForRaffleOnce(raffleId: Long): List<Int> =
        assignedNumberDao.getAssignedNumbersForRaffleOnce(raffleId)

    override suspend fun getAllAssignedForRaffle(raffleId: Long): List<AssignedNumberEntity> =
        assignedNumberDao.getAllAssignedForRaffle(raffleId)

    override fun getNumbersForParticipant(participantId: Long): Flow<List<AssignedNumberEntity>> =
        assignedNumberDao.getNumbersForParticipant(participantId)

    override suspend fun getNumbersForParticipantOnce(participantId: Long): List<AssignedNumberEntity> =
        assignedNumberDao.getNumbersForParticipantOnce(participantId)

    override suspend fun isNumberTaken(raffleId: Long, number: Int): Boolean =
        assignedNumberDao.isNumberTaken(raffleId, number) > 0

    override suspend fun assignNumber(participantId: Long, raffleId: Long, number: Int): Result<Unit> =
        runCatching {
            assignedNumberDao.insertAssignedNumber(
                AssignedNumberEntity(participantId = participantId, raffleId = raffleId, number = number)
            )
            Unit
        }

    override suspend fun assignMultiple(participantId: Long, raffleId: Long, numbers: List<Int>): Result<Unit> =
        runCatching {
            assignedNumberDao.insertAll(
                numbers.map {
                    AssignedNumberEntity(participantId = participantId, raffleId = raffleId, number = it)
                }
            )
        }

    override suspend fun unassignNumber(entity: AssignedNumberEntity) =
        assignedNumberDao.deleteAssignedNumber(entity)

    override suspend fun unassignAllForParticipant(participantId: Long) =
        assignedNumberDao.deleteAllForParticipant(participantId)
}
