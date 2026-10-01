package com.antigravity.mobile.di

import com.antigravity.mobile.data.remote.SignalApiService
import com.antigravity.mobile.data.remote.SignalApiServiceImpl
import com.antigravity.mobile.data.repository.ApiAuthRepository
import com.antigravity.mobile.data.repository.ApiGameRepository
import com.antigravity.mobile.data.repository.ApiMarketRepository
import com.antigravity.mobile.data.repository.SignalRepositoryImpl
import com.antigravity.mobile.domain.repository.AuthRepository
import com.antigravity.mobile.domain.repository.GameRepository
import com.antigravity.mobile.domain.repository.MarketRepository
import com.antigravity.mobile.domain.repository.SignalRepository
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

    @Binds
    @Singleton
    abstract fun bindSignalApiService(
        signalApiServiceImpl: SignalApiServiceImpl
    ): SignalApiService

    @Binds
    @Singleton
    abstract fun bindSignalRepository(
        signalRepositoryImpl: SignalRepositoryImpl
    ): SignalRepository
}
