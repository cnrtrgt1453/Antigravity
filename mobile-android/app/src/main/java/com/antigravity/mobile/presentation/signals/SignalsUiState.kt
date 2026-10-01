package com.antigravity.mobile.presentation.signals

import com.antigravity.mobile.domain.model.TradingSignal

sealed interface SignalsUiState {
    object Loading : SignalsUiState
    data class Success(val signals: List<TradingSignal>) : SignalsUiState
    data class Error(val message: String) : SignalsUiState
}
