package com.easyrifa.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.easyrifa.data.db.entity.RaffleEntity
import com.easyrifa.data.db.model.RaffleWithParticipants
import kotlinx.coroutines.flow.Flow

@Dao
interface RaffleDao {

    @Query("SELECT * FROM raffles ORDER BY createdAt DESC")
    fun getAllRaffles(): Flow<List<RaffleEntity>>

    @Query("SELECT * FROM raffles WHERE id = :raffleId")
    fun getRaffleById(raffleId: Long): Flow<RaffleEntity?>

    @Transaction
    @Query("SELECT * FROM raffles WHERE id = :raffleId")
    fun getRaffleWithParticipants(raffleId: Long): Flow<RaffleWithParticipants?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRaffle(raffle: RaffleEntity): Long

    @Update
    suspend fun updateRaffle(raffle: RaffleEntity)

    @Delete
    suspend fun deleteRaffle(raffle: RaffleEntity)
}
