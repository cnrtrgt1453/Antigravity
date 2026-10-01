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
import java.util.Calendar
import javax.inject.Inject

data class NewsUiState(
    val news: List<News> = emptyList(),
    val isLoading: Boolean = false,
    val refreshing: Boolean = false,
    val watchlistOnly: Boolean = false,
    val selectedSymbol: String? = null,
    val searchQuery: String = "",
    val sortOrder: String = "desc", // "desc": Yeniden Eskiye, "asc": Eskiden Yeniye
    val currentMonthName: String = "",
    val error: String? = null
)

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val marketRepository: MarketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState(currentMonthName = getCurrentMonthName()))
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private var currentPage = 0

    init {
        loadNews(reset = true)
    }

    private fun getCurrentMonthName(): String {
        val months = listOf(
            "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
            "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
        )
        val cal = Calendar.getInstance()
        val monthIdx = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        return "${months.getOrElse(monthIdx) { "" }} $year"
    }

    fun loadNews(reset: Boolean = false) {
        if (reset) {
            currentPage = 0
            _uiState.value = _uiState.value.copy(news = emptyList())
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = !reset, refreshing = reset)

            val symbol = _uiState.value.selectedSymbol
            val sort = _uiState.value.sortOrder

            marketRepository.getNews(
                page = currentPage,
                watchlistOnly = _uiState.value.watchlistOnly,
                symbol = symbol,
                sort = sort
            ).onSuccess { newNews ->
                _uiState.value = _uiState.value.copy(
                    news = if (reset) newNews else _uiState.value.news + newNews,
                    isLoading = false,
                    refreshing = false,
                    error = null
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

    fun refreshNews() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(refreshing = true)
            try {
                marketRepository.syncKapNews()
            } catch (_: Exception) {}
            loadNews(reset = true)
        }
    }

    fun selectSymbol(symbol: String?) {
        val clean = symbol?.trim()?.uppercase()?.removeSuffix(".IS")?.ifBlank { null }
        _uiState.value = _uiState.value.copy(selectedSymbol = clean, searchQuery = clean ?: "")
        loadNews(reset = true)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun submitSearch() {
        val query = _uiState.value.searchQuery.trim().uppercase().removeSuffix(".IS")
        selectSymbol(query.ifBlank { null })
    }

    fun setSortOrder(order: String) {
        if (_uiState.value.sortOrder != order) {
            _uiState.value = _uiState.value.copy(sortOrder = order)
            loadNews(reset = true)
        }
    }
}
