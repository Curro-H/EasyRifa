package com.easyrifa.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "drawn_numbers",
    foreignKeys = [
        ForeignKey(
            entity = DrawResultEntity::class,
            parentColumns = ["id"],
            childColumns = ["drawResultId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["participantId"],
            onDelete = ForeignKey.SET_DEFAULT
        )
    ],
    indices = [Index("drawResultId"), Index("participantId")]
)
data class DrawnNumberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val drawResultId: Long,
    val number: Int,
    val participantId: Long = 0,
    val participantName: String = ""
)
