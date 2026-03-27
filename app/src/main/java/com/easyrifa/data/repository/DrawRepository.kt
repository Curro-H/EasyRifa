package com.easyrifa.data.repository

import com.easyrifa.data.db.dao.DrawResultDao
import com.easyrifa.data.db.dao.DrawnNumberDao
import com.easyrifa.data.db.entity.DrawResultEntity
import com.easyrifa.data.db.entity.DrawnNumberEntity
import com.easyrifa.data.db.model.DrawResultWithWinners
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface DrawRepository {
    fun getDrawResultsWithWinners(raffleId: Long): Flow<List<DrawResultWithWinners>>
    suspend fun saveDrawResult(raffleId: Long, numberOfWinners: Int): Long
    suspend fun saveDrawnNumbers(drawnNumbers: List<DrawnNumberEntity>)
    suspend fun deleteDrawResult(drawResult: DrawResultEntity)
}

@Singleton
class DrawRepositoryImpl @Inject constructor(
    private val drawResultDao: DrawResultDao,
    private val drawnNumberDao: DrawnNumberDao
) : DrawRepository {

    override fun getDrawResultsWithWinners(raffleId: Long): Flow<List<DrawResultWithWinners>> =
        drawResultDao.getDrawResultsWithWinners(raffleId)

    override suspend fun saveDrawResult(raffleId: Long, numberOfWinners: Int): Long =
        drawResultDao.insertDrawResult(
            DrawResultEntity(raffleId = raffleId, numberOfWinners = numberOfWinners)
        )

    override suspend fun saveDrawnNumbers(drawnNumbers: List<DrawnNumberEntity>) =
        drawnNumberDao.insertAll(drawnNumbers)

    override suspend fun deleteDrawResult(drawResult: DrawResultEntity) =
        drawResultDao.deleteDrawResult(drawResult)
}
