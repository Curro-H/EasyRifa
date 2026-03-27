package com.easyrifa.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "participants",
    foreignKeys = [
        ForeignKey(
            entity = RaffleEntity::class,
            parentColumns = ["id"],
            childColumns = ["raffleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("raffleId")]
)
data class ParticipantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val raffleId: Long,
    val name: String
)
