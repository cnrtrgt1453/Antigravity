package com.antigravity.mobile.data.repository

import com.antigravity.mobile.data.mapper.toDomain
import com.antigravity.mobile.data.remote.SignalApiService
import com.antigravity.mobile.domain.model.TradingSignal
import com.antigravity.mobile.domain.repository.SignalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SignalRepositoryImpl @Inject constructor(
    private val apiService: SignalApiService
) : SignalRepository {

    override fun getActiveSignals(): Flow<Result<List<TradingSignal>>> = flow {
        try {
            val dtoList = apiService.fetchActiveSignals()
            val domainList = dtoList.map { it.toDomain() }
            emit(Result.success(domainList))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
