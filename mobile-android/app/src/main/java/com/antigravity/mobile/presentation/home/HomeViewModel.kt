package com.antigravity.mobile.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.mobile.domain.model.MarketSummary
import com.antigravity.mobile.domain.model.OHLCData
import com.antigravity.mobile.domain.model.Stock
import com.antigravity.mobile.domain.repository.MarketRepository
import com.antigravity.mobile.domain.usecase.GetOHLCDataUseCase
import com.antigravity.mobile.domain.usecase.ToggleWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.antigravity.mobile.data.config.SupabaseConfig
import javax.inject.Inject

data class HomeState(
    val summaries: List<MarketSummary> = emptyList(),
    val bist30Stocks: List<Stock> = emptyList(),
    val isBist30Loading: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedStock: Stock? = null,
    val chartData: OHLCData = OHLCData(),
    val isChartLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MarketRepository,
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase,
    private val getOHLCDataUseCase: GetOHLCDataUseCase,
    private val config: SupabaseConfig
) : ViewModel() {

    fun isLoggedIn(): Boolean = config.isLoggedIn()

    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()

    private var refreshJob: kotlinx.coroutines.Job? = null

    init {
        fetchHomeData()
        fetchBist30Stocks()
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                fetchHomeData()
                fetchBist30Stocks()
                kotlinx.coroutines.delay(30000) // 30 seconds
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }

    fun fetchHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = _uiState.value.summaries.isEmpty())
            repository.getMarketSummary()
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(summaries = list, isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message, isLoading = false)
                }
        }
    }

    fun fetchBist30Stocks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBist30Loading = _uiState.value.bist30Stocks.isEmpty())
            repository.getBist30Stocks()
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(bist30Stocks = list, isBist30Loading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isBist30Loading = false)
                }
        }
    }

    fun toggleWatchlist(stock: Stock) {
        val willBeWatched = !stock.isWatched
        val updated = _uiState.value.bist30Stocks.map {
            if (it.symbol == stock.symbol || it.symbol.substringBefore(".") == stock.symbol.substringBefore(".")) {
                it.copy(isWatched = willBeWatched)
            } else {
                it
            }
        }
        _uiState.value = _uiState.value.copy(bist30Stocks = updated)

        viewModelScope.launch {
            toggleWatchlistUseCase(stock.symbol, willBeWatched)
        }
    }

    fun selectStock(stock: Stock?) {
        _uiState.value = _uiState.value.copy(selectedStock = stock)
        if (stock != null) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isChartLoading = true)
                getOHLCDataUseCase(stock.symbol)
                    .onSuccess { data ->
                        _uiState.value = _uiState.value.copy(chartData = data, isChartLoading = false)
                    }
                    .onFailure {
                        _uiState.value = _uiState.value.copy(isChartLoading = false)
                    }
            }
        }
    }
}
