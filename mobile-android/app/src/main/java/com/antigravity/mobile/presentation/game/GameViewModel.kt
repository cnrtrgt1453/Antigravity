package com.antigravity.mobile.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.antigravity.mobile.domain.model.Portfolio
import com.antigravity.mobile.domain.model.MarketSignal
import com.antigravity.mobile.domain.model.Stock
import com.antigravity.mobile.domain.model.MarketFilter
import com.antigravity.mobile.domain.repository.GameRepository
import com.antigravity.mobile.domain.repository.MarketRepository
import com.antigravity.mobile.data.config.SupabaseConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameState(
    val portfolio: Portfolio? = null,
    val marketData: List<MarketSignal> = emptyList(),
    val stockPrices: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val marketRepository: MarketRepository,
    private val config: SupabaseConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameState())
    val uiState = _uiState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(config.isLoggedIn())
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(MarketFilter.ALL)
    val selectedFilter = _selectedFilter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val stocksPagingData: Flow<PagingData<Stock>> = combine(
        _selectedFilter,
        _searchQuery.debounce { if (it.isEmpty()) 0L else 300L }
    ) { filter, query ->
        Pair(filter, query)
    }.flatMapLatest { (filter, query) ->
        marketRepository.getStocksPagingData(filter = filter, searchQuery = query)
    }.cachedIn(viewModelScope)

    init {
        if (config.isLoggedIn()) {
            refreshAll()
        }
    }

    fun checkLoginStatus() {
        val loggedIn = config.isLoggedIn()
        _isLoggedIn.value = loggedIn
        if (loggedIn) {
            refreshAll()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: MarketFilter) {
        _selectedFilter.value = filter
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Parallel fetch
            val portfolioResult = gameRepository.getPortfolio()
            val marketResult = marketRepository.getLatestSignals()
            val bist30Result = marketRepository.getBist30Stocks()

            val priceMap = mutableMapOf<String, Double>()
            bist30Result.getOrNull()?.forEach { stock ->
                val clean = stock.symbol.substringBefore(".")
                stock.currentPrice?.let { priceMap[clean] = it }
            }

            _uiState.value = _uiState.value.copy(
                portfolio = portfolioResult.getOrNull(),
                marketData = marketResult.getOrNull() ?: emptyList(),
                stockPrices = priceMap,
                isLoading = false,
                error = portfolioResult.exceptionOrNull()?.message
            )
        }
    }

    fun trade(symbol: String, quantity: Long, price: Double, type: String, onDone: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            val result = if (type == "BUY") {
                gameRepository.buyStock(symbol, quantity, price)
            } else {
                gameRepository.sellStock(symbol, quantity, price)
            }

            result.onSuccess { updatedPortfolio ->
                val msg = if (type == "BUY") {
                    "$quantity lot $symbol başarıyla portföyünüze eklendi!"
                } else {
                    "$quantity lot $symbol satışı başarıyla tamamlandı!"
                }
                _uiState.value = _uiState.value.copy(
                    portfolio = updatedPortfolio,
                    isLoading = false,
                    successMessage = msg,
                    error = null
                )
                onDone?.invoke(true, msg)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    error = error.message ?: "İşlem sırasında bir hata oluştu.",
                    isLoading = false
                )
                onDone?.invoke(false, error.message)
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
