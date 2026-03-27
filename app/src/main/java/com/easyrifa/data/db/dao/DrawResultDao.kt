package com.easyrifa.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.easyrifa.data.db.entity.DrawResultEntity
import com.easyrifa.data.db.model.DrawResultWithWinners
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawResultDao {

    @Query("SELECT * FROM draw_results WHERE raffleId = :raffleId ORDER BY timestamp DESC")
    fun getDrawResultsForRaffle(raffleId: Long): Flow<List<DrawResultEntity>>

    @Transaction
    @Query("SELECT * FROM draw_results WHERE raffleId = :raffleId ORDER BY timestamp DESC")
    fun getDrawResultsWithWinners(raffleId: Long): Flow<List<DrawResultWithWinners>>

    @Insert
    suspend fun insertDrawResult(drawResult: DrawResultEntity): Long

    @Delete
    suspend fun deleteDrawResult(drawResult: DrawResultEntity)
}
