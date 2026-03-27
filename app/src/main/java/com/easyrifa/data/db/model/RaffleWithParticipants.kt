package com.easyrifa.data.db.model

import androidx.room.Embedded
import androidx.room.Relation
import com.easyrifa.data.db.entity.ParticipantEntity
import com.easyrifa.data.db.entity.RaffleEntity

data class RaffleWithParticipants(
    @Embedded val raffle: RaffleEntity,
    @Relation(
        entity = ParticipantEntity::class,
        parentColumn = "id",
        entityColumn = "raffleId"
    )
    val participants: List<ParticipantWithNumbers>
)
