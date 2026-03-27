package com.easyrifa.data.repository

import com.easyrifa.data.db.dao.ParticipantDao
import com.easyrifa.data.db.entity.ParticipantEntity
import com.easyrifa.data.db.model.ParticipantWithNumbers
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface ParticipantRepository {
    fun getParticipantsByRaffle(raffleId: Long): Flow<List<ParticipantEntity>>
    fun getParticipantWithNumbers(participantId: Long): Flow<ParticipantWithNumbers?>
    suspend fun getParticipantById(participantId: Long): ParticipantEntity?
    suspend fun getParticipantsByRaffleOnce(raffleId: Long): List<ParticipantEntity>
    suspend fun addParticipant(raffleId: Long, name: String): Long
    suspend fun updateParticipant(participant: ParticipantEntity)
    suspend fun deleteParticipant(participant: ParticipantEntity)
}

@Singleton
class ParticipantRepositoryImpl @Inject constructor(
    private val participantDao: ParticipantDao
) : ParticipantRepository {

    override fun getParticipantsByRaffle(raffleId: Long): Flow<List<ParticipantEntity>> =
        participantDao.getParticipantsByRaffle(raffleId)

    override fun getParticipantWithNumbers(participantId: Long): Flow<ParticipantWithNumbers?> =
        participantDao.getParticipantWithNumbers(participantId)

    override suspend fun getParticipantById(participantId: Long): ParticipantEntity? =
        participantDao.getParticipantById(participantId)

    override suspend fun getParticipantsByRaffleOnce(raffleId: Long): List<ParticipantEntity> =
        participantDao.getParticipantsByRaffleOnce(raffleId)

    override suspend fun addParticipant(raffleId: Long, name: String): Long =
        participantDao.insertParticipant(ParticipantEntity(raffleId = raffleId, name = name))

    override suspend fun updateParticipant(participant: ParticipantEntity) =
        participantDao.updateParticipant(participant)

    override suspend fun deleteParticipant(participant: ParticipantEntity) =
        participantDao.deleteParticipant(participant)
}
