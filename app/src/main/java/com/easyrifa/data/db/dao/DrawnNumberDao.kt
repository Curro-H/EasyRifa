package com.easyrifa.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import com.easyrifa.data.db.entity.DrawnNumberEntity

@Dao
interface DrawnNumberDao {

    @Insert
    suspend fun insertAll(drawnNumbers: List<DrawnNumberEntity>)
}
