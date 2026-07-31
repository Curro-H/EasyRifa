package com.easyrifa.di

import android.content.Context
import androidx.room.Room
import com.easyrifa.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()

    @Provides
    fun provideRaffleDao(db: AppDatabase) = db.raffleDao()

    @Provides
    fun provideParticipantDao(db: AppDatabase) = db.participantDao()

    @Provides
    fun provideAssignedNumberDao(db: AppDatabase) = db.assignedNumberDao()

    @Provides
    fun provideDrawResultDao(db: AppDatabase) = db.drawResultDao()

    @Provides
    fun provideDrawnNumberDao(db: AppDatabase) = db.drawnNumberDao()
}
