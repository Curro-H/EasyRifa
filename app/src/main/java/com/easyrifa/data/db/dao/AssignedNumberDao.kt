package com.easyrifa.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.easyrifa.data.db.entity.AssignedNumberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignedNumberDao {

    @Query("SELECT number FROM assigned_numbers WHERE raffleId = :raffleId")
    fun getAssignedNumbersForRaffle(raffleId: Long): Flow<List<Int>>

    @Query("SELECT number FROM assigned_numbers WHERE raffleId = :raffleId")
    suspend fun getAssignedNumbersForRaffleOnce(raffleId: Long): List<Int>

    @Query("SELECT * FROM assigned_numbers WHERE raffleId = :raffleId")
    suspend fun getAllAssignedForRaffle(raffleId: Long): List<AssignedNumberEntity>

    @Query("SELECT * FROM assigned_numbers WHERE participantId = :participantId")
    fun getNumbersForParticipant(participantId: Long): Flow<List<AssignedNumberEntity>>

    @Query("SELECT * FROM assigned_numbers WHERE participantId = :participantId")
    suspend fun getNumbersForParticipantOnce(participantId: Long): List<AssignedNumberEntity>

    @Query("SELECT COUNT(*) FROM assigned_numbers WHERE raffleId = :raffleId AND number = :number")
    suspend fun isNumberTaken(raffleId: Long, number: Int): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAssignedNumber(assignedNumber: AssignedNumberEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(numbers: List<AssignedNumberEntity>)

    @Delete
    suspend fun deleteAssignedNumber(assignedNumber: AssignedNumberEntity)

    @Query("DELETE FROM assigned_numbers WHERE participantId = :participantId")
    suspend fun deleteAllForParticipant(participantId: Long)

    @Query("SELECT COUNT(*) FROM assigned_numbers WHERE raffleId = :raffleId")
    fun getAssignedCountForRaffle(raffleId: Long): Flow<Int>
}
