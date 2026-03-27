package com.easyrifa.di

import com.easyrifa.data.repository.AssignedNumberRepository
import com.easyrifa.data.repository.AssignedNumberRepositoryImpl
import com.easyrifa.data.repository.DrawRepository
import com.easyrifa.data.repository.DrawRepositoryImpl
import com.easyrifa.data.repository.ParticipantRepository
import com.easyrifa.data.repository.ParticipantRepositoryImpl
import com.easyrifa.data.repository.RaffleRepository
import com.easyrifa.data.repository.RaffleRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRaffleRepository(impl: RaffleRepositoryImpl): RaffleRepository

    @Binds
    @Singleton
    abstract fun bindParticipantRepository(impl: ParticipantRepositoryImpl): ParticipantRepository

    @Binds
    @Singleton
    abstract fun bindAssignedNumberRepository(impl: AssignedNumberRepositoryImpl): AssignedNumberRepository

    @Binds
    @Singleton
    abstract fun bindDrawRepository(impl: DrawRepositoryImpl): DrawRepository
}
