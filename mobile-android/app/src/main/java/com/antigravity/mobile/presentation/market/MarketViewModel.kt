package com.antigravity.mobile.presentation.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.antigravity.mobile.domain.model.Stock
import com.antigravity.mobile.domain.model.OHLCData
import com.antigravity.mobile.domain.model.MarketFilter
import com.antigravity.mobile.domain.repository.MarketRepository
import com.antigravity.mobile.domain.usecase.GetOHLCDataUseCase
import com.antigravity.mobile.domain.usecase.ToggleWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.antigravity.mobile.data.config.SupabaseConfig
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val repository: MarketRepository,
    private val getOHLCDataUseCase: GetOHLCDataUseCase,
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase,
    private val config: SupabaseConfig
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(MarketFilter.ALL)
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _watchedSymbols = MutableStateFlow<Set<String>>(emptySet())
    val watchedSymbols = _watchedSymbols.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(config.isLoggedIn())
    val isLoggedIn = _isLoggedIn.asStateFlow()

    fun checkLoginStatus() {
        _isLoggedIn.value = config.isLoggedIn()
    }

    init {
        loadWatchlist()
    }

    fun loadWatchlist() {
        viewModelScope.launch {
            repository.getWatchlist().onSuccess { list ->
                _watchedSymbols.value = list.toSet()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val stocksPagingData: Flow<PagingData<Stock>> = combine(
        _selectedFilter,
        _searchQuery.debounce { if (it.isEmpty()) 0L else 300L }
    ) { filter, query ->
        Pair(filter, query)
    }.flatMapLatest { (filter, query) ->
        repository.getStocksPagingData(filter, query)
    }.cachedIn(viewModelScope)

    fun setFilter(filter: MarketFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _selectedStock = MutableStateFlow<Stock?>(null)
    val selectedStock = _selectedStock.asStateFlow()

    private val _chartData = MutableStateFlow(OHLCData())
    val chartData = _chartData.asStateFlow()

    private val _isChartLoading = MutableStateFlow(false)
    val isChartLoading = _isChartLoading.asStateFlow()

    fun toggleWatchlist(stock: Stock) {
        val currentlyWatched = _watchedSymbols.value.contains(stock.symbol) ||
                _watchedSymbols.value.contains(stock.symbol.substringBefore(".")) ||
                stock.isWatched

        val willBeWatched = !currentlyWatched
        val newSet = _watchedSymbols.value.toMutableSet()
        val cleanSymbol = stock.symbol.trim().uppercase()
        val baseSymbol = cleanSymbol.substringBefore(".")

        if (willBeWatched) {
            newSet.add(cleanSymbol)
        } else {
            newSet.remove(cleanSymbol)
            newSet.remove(baseSymbol)
            newSet.remove("$baseSymbol.IS")
        }
        _watchedSymbols.value = newSet

        viewModelScope.launch {
            toggleWatchlistUseCase(stock.symbol, willBeWatched)
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
            getOHLCDataUseCase(symbol)
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
