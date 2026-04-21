package com.antigravity.mobile.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.mobile.domain.model.TradeHistory
import com.antigravity.mobile.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryState(
    val results: List<TradeHistory> = emptyList(),
    val filteredResults: List<TradeHistory> = emptyList(),
    val isLoading: Boolean = false,
    val activeFilter: HistoryFilter = HistoryFilter.ALL
)

enum class HistoryFilter { ALL, BUY, SELL }

@HiltViewModel
class TradeHistoryViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchHistory()
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getTradeHistory()
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(results = list, isLoading = false)
                    applyFilter(_uiState.value.activeFilter)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

    fun applyFilter(filter: HistoryFilter) {
        val filtered = when (filter) {
            HistoryFilter.ALL -> _uiState.value.results
            HistoryFilter.BUY -> _uiState.value.results.filter { it.type == "BUY" }
            HistoryFilter.SELL -> _uiState.value.results.filter { it.type == "SELL" }
        }
        _uiState.value = _uiState.value.copy(activeFilter = filter, filteredResults = filtered)
    }
}
