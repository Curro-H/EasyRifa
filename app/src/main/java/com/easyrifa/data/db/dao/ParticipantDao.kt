package com.easyrifa.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.easyrifa.data.db.entity.ParticipantEntity
import com.easyrifa.data.db.model.ParticipantWithNumbers
import kotlinx.coroutines.flow.Flow

@Dao
interface ParticipantDao {

    @Query("SELECT * FROM participants WHERE raffleId = :raffleId ORDER BY name ASC")
    fun getParticipantsByRaffle(raffleId: Long): Flow<List<ParticipantEntity>>

    @Transaction
    @Query("SELECT * FROM participants WHERE id = :participantId")
    fun getParticipantWithNumbers(participantId: Long): Flow<ParticipantWithNumbers?>

    @Query("SELECT * FROM participants WHERE id = :participantId")
    suspend fun getParticipantById(participantId: Long): ParticipantEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertParticipant(participant: ParticipantEntity): Long

    @Update
    suspend fun updateParticipant(participant: ParticipantEntity)

    @Delete
    suspend fun deleteParticipant(participant: ParticipantEntity)

    @Query("SELECT * FROM participants WHERE raffleId = :raffleId")
    suspend fun getParticipantsByRaffleOnce(raffleId: Long): List<ParticipantEntity>
}
