package com.antigravity.mobile.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.mobile.domain.model.Portfolio
import com.antigravity.mobile.domain.model.MarketSignal
import com.antigravity.mobile.domain.repository.GameRepository
import com.antigravity.mobile.domain.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameState(
    val portfolio: Portfolio? = null,
    val marketData: List<MarketSignal> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val marketRepository: MarketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Parallel fetch
            val portfolioResult = gameRepository.getPortfolio()
            val marketResult = marketRepository.getLatestSignals()

            _uiState.value = _uiState.value.copy(
                portfolio = portfolioResult.getOrNull(),
                marketData = marketResult.getOrNull() ?: emptyList(),
                isLoading = false,
                error = portfolioResult.exceptionOrNull()?.message
            )
        }
    }

    fun trade(symbol: String, quantity: Long, price: Double, type: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = if (type == "BUY") {
                gameRepository.buyStock(symbol, quantity, price)
            } else {
                gameRepository.sellStock(symbol, quantity, price)
            }

            result.onSuccess { updatedPortfolio ->
                _uiState.value = _uiState.value.copy(portfolio = updatedPortfolio, isLoading = false)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message, isLoading = false)
            }
        }
    }
}
