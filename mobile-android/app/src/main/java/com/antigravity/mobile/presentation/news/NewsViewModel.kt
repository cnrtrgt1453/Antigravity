package com.antigravity.mobile.presentation.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.mobile.domain.model.News
import com.antigravity.mobile.domain.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewsUiState(
    val news: List<News> = emptyList(),
    val isLoading: Boolean = false,
    val refreshing: Boolean = false,
    val watchlistOnly: Boolean = true,
    val selectedSymbol: String? = null,
    val sortOrder: String = "publishedAt,desc",
    val error: String? = null
)

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val marketRepository: MarketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private var currentPage = 0

    init {
        loadNews(reset = true)
    }

    fun loadNews(reset: Boolean = false) {
        if (reset) {
            currentPage = 0
            _uiState.value = _uiState.value.copy(news = emptyList())
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = !reset, refreshing = reset)
            
            marketRepository.getNews(
                page = currentPage,
                watchlistOnly = _uiState.value.watchlistOnly,
                symbol = _uiState.value.selectedSymbol,
                sort = _uiState.value.sortOrder
            ).onSuccess { newNews ->
                _uiState.value = _uiState.value.copy(
                    news = _uiState.value.news + newNews,
                    isLoading = false,
                    refreshing = false
                )
                currentPage++
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    error = error.message,
                    isLoading = false,
                    refreshing = false
                )
            }
        }
    }

    fun toggleWatchlistOnly(only: Boolean) {
        _uiState.value = _uiState.value.copy(watchlistOnly = only, selectedSymbol = null)
        loadNews(reset = true)
    }

    fun selectSymbol(symbol: String?) {
        _uiState.value = _uiState.value.copy(selectedSymbol = symbol)
        loadNews(reset = true)
    }

    fun toggleSortOrder() {
        val newOrder = if (_uiState.value.sortOrder.endsWith("desc")) "publishedAt,asc" else "publishedAt,desc"
        _uiState.value = _uiState.value.copy(sortOrder = newOrder)
        loadNews(reset = true)
    }
}
