package com.easyrifa.data.db.model

import androidx.room.Embedded
import androidx.room.Relation
import com.easyrifa.data.db.entity.DrawResultEntity
import com.easyrifa.data.db.entity.DrawnNumberEntity

data class DrawResultWithWinners(
    @Embedded val drawResult: DrawResultEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "drawResultId"
    )
    val drawnNumbers: List<DrawnNumberEntity>
)
