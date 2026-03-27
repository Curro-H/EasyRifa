package com.easyrifa.data.db.model

import androidx.room.Embedded
import androidx.room.Relation
import com.easyrifa.data.db.entity.AssignedNumberEntity
import com.easyrifa.data.db.entity.ParticipantEntity

data class ParticipantWithNumbers(
    @Embedded val participant: ParticipantEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "participantId"
    )
    val numbers: List<AssignedNumberEntity>
)
