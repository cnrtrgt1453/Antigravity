package com.antigravity.mobile.presentation.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.antigravity.mobile.domain.model.Stock
import com.antigravity.mobile.domain.model.OHLCData
import com.antigravity.mobile.domain.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val repository: MarketRepository
) : ViewModel() {

    val stocksPagingData: Flow<PagingData<Stock>> = repository
        .getStocksPagingData()
        .cachedIn(viewModelScope)

    private val _selectedStock = MutableStateFlow<Stock?>(null)
    val selectedStock = _selectedStock.asStateFlow()

    private val _chartData = MutableStateFlow(OHLCData())
    val chartData = _chartData.asStateFlow()

    private val _isChartLoading = MutableStateFlow(false)
    val isChartLoading = _isChartLoading.asStateFlow()

    fun toggleWatchlist(stock: Stock) {
        viewModelScope.launch {
            repository.toggleWatchlist(stock.symbol, !stock.isWatched)
        }
    }

    fun selectStock(stock: Stock?) {
        _selectedStock.value = stock
        if (stock != null) {
            loadChartData(stock.symbol)
        }
    }

    private fun loadChartData(symbol: String) {
        viewModelScope.launch {
            _isChartLoading.value = true
            repository.getOHLCData(symbol)
                .onSuccess { data ->
                    _chartData.value = data
                }
                .onFailure {
                    // Handle error
                }
            _isChartLoading.value = false
        }
    }
}
