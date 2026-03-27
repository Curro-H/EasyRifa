package com.easyrifa.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "raffles")
data class RaffleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val minNumber: Int,
    val maxNumber: Int,
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
