package com.easyrifa.data.repository

import com.easyrifa.data.db.dao.RaffleDao
import com.easyrifa.data.db.entity.RaffleEntity
import com.easyrifa.data.db.model.RaffleWithParticipants
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface RaffleRepository {
    fun getAllRaffles(): Flow<List<RaffleEntity>>
    fun getRaffleById(raffleId: Long): Flow<RaffleEntity?>
    fun getRaffleWithParticipants(raffleId: Long): Flow<RaffleWithParticipants?>
    suspend fun createRaffle(name: String, min: Int, max: Int, imagePath: String?): Long
    suspend fun updateRaffle(raffle: RaffleEntity)
    suspend fun deleteRaffle(raffle: RaffleEntity)
}

@Singleton
class RaffleRepositoryImpl @Inject constructor(
    private val raffleDao: RaffleDao
) : RaffleRepository {

    override fun getAllRaffles(): Flow<List<RaffleEntity>> =
        raffleDao.getAllRaffles()

    override fun getRaffleById(raffleId: Long): Flow<RaffleEntity?> =
        raffleDao.getRaffleById(raffleId)

    override fun getRaffleWithParticipants(raffleId: Long): Flow<RaffleWithParticipants?> =
        raffleDao.getRaffleWithParticipants(raffleId)

    override suspend fun createRaffle(name: String, min: Int, max: Int, imagePath: String?): Long =
        raffleDao.insertRaffle(
            RaffleEntity(name = name, minNumber = min, maxNumber = max, imagePath = imagePath)
        )

    override suspend fun updateRaffle(raffle: RaffleEntity) =
        raffleDao.updateRaffle(raffle)

    override suspend fun deleteRaffle(raffle: RaffleEntity) =
        raffleDao.deleteRaffle(raffle)
}
