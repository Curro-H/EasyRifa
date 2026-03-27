package com.easyrifa.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assigned_numbers",
    foreignKeys = [
        ForeignKey(
            entity = RaffleEntity::class,
            parentColumns = ["id"],
            childColumns = ["raffleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["participantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("raffleId"),
        Index("participantId"),
        // A number can only be assigned once per raffle
        Index(value = ["raffleId", "number"], unique = true)
    ]
)
data class AssignedNumberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val participantId: Long,
    val raffleId: Long,
    val number: Int
)
