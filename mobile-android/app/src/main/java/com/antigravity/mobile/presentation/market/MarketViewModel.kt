package com.antigravity.mobile.presentation.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.antigravity.mobile.domain.model.Stock
import com.antigravity.mobile.domain.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val repository: MarketRepository
) : ViewModel() {

    val stocksPagingData: Flow<PagingData<Stock>> = repository
        .getStocksPagingData()
        .cachedIn(viewModelScope)

    fun toggleWatchlist(stock: Stock) {
        viewModelScope.launch {
            repository.toggleWatchlist(stock.symbol, !stock.isWatched)
        }
    }
}
