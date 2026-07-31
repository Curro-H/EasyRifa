package com.easyrifa.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.easyrifa.data.db.dao.AssignedNumberDao
import com.easyrifa.data.db.dao.DrawResultDao
import com.easyrifa.data.db.dao.DrawnNumberDao
import com.easyrifa.data.db.dao.ParticipantDao
import com.easyrifa.data.db.dao.RaffleDao
import com.easyrifa.data.db.entity.AssignedNumberEntity
import com.easyrifa.data.db.entity.DrawResultEntity
import com.easyrifa.data.db.entity.DrawnNumberEntity
import com.easyrifa.data.db.entity.ParticipantEntity
import com.easyrifa.data.db.entity.RaffleEntity

@Database(
    entities = [
        RaffleEntity::class,
        ParticipantEntity::class,
        AssignedNumberEntity::class,
        DrawResultEntity::class,
        DrawnNumberEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun raffleDao(): RaffleDao
    abstract fun participantDao(): ParticipantDao
    abstract fun assignedNumberDao(): AssignedNumberDao
    abstract fun drawResultDao(): DrawResultDao
    abstract fun drawnNumberDao(): DrawnNumberDao

    companion object {
        const val DATABASE_NAME = "easyrifa.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE raffles ADD COLUMN pricePerNumber REAL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE raffles ADD COLUMN drawDate INTEGER")
            }
        }
    }
}
