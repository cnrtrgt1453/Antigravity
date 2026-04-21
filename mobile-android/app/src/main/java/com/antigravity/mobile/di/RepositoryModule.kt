package com.antigravity.mobile.di

import com.antigravity.mobile.data.repository.ApiAuthRepository
import com.antigravity.mobile.domain.repository.AuthRepository
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
    abstract fun bindAuthRepository(
        apiAuthRepository: ApiAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMarketRepository(
        apiMarketRepository: ApiMarketRepository
    ): MarketRepository

    @Binds
    @Singleton
    abstract fun bindGameRepository(
        apiGameRepository: ApiGameRepository
    ): GameRepository
}
