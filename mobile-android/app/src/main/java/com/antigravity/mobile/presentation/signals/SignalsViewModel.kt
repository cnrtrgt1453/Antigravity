package com.antigravity.mobile.presentation.signals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.mobile.domain.model.OHLCData
import com.antigravity.mobile.domain.model.MarketSignal
import com.antigravity.mobile.domain.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignalsState(
    val results: List<MarketSignal> = emptyList(),
    val filteredResults: List<MarketSignal> = emptyList(),
    val isLoading: Boolean = false,
    val activeFilter: SignalFilter = SignalFilter.ALL,
    val selectedSymbol: String? = null,
    val chartData: OHLCData = OHLCData(),
    val isChartLoading: Boolean = false
)

enum class SignalFilter { ALL, GOLDEN, DEAD }

@HiltViewModel
class SignalsViewModel @Inject constructor(
    private val repository: MarketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignalsState())
    val uiState = _uiState.asStateFlow()

    private var refreshJob: kotlinx.coroutines.Job? = null

    init {
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                fetchSignals()
                kotlinx.coroutines.delay(60000) // 60 seconds
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }

    fun fetchSignals() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getLatestSignals()
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(results = list, isLoading = false)
                    applyFilter(_uiState.value.activeFilter)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

    fun applyFilter(filter: SignalFilter) {
        val filtered = when (filter) {
            SignalFilter.ALL -> _uiState.value.results
            SignalFilter.GOLDEN -> _uiState.value.results.filter { it.signal == "GOLDEN_CROSS" }
            SignalFilter.DEAD -> _uiState.value.results.filter { it.signal == "DEAD_CROSS" }
        }
        _uiState.value = _uiState.value.copy(activeFilter = filter, filteredResults = filtered)
    }

    fun selectSymbol(symbol: String?) {
        _uiState.value = _uiState.value.copy(selectedSymbol = symbol)
        if (symbol != null) {
            loadChartData(symbol)
        }
    }

    private fun loadChartData(symbol: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChartLoading = true)
            repository.getOHLCData(symbol)
                .onSuccess { data ->
                    _uiState.value = _uiState.value.copy(chartData = data, isChartLoading = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isChartLoading = false)
                }
        }
    }
}
