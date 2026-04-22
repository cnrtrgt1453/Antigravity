package com.antigravity.mobile.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.mobile.domain.model.CooldownStatus
import com.antigravity.mobile.domain.model.MarketSummary
import com.antigravity.mobile.domain.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val summaries: List<MarketSummary> = emptyList(),
    val isLoading: Boolean = false,
    val scanLoading: Boolean = false,
    val cooldownStatus: CooldownStatus? = null,
    val scanResult: String? = null,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MarketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()

    private var refreshJob: kotlinx.coroutines.Job? = null

    init {
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                fetchHomeData()
                fetchCooldown()
                kotlinx.coroutines.delay(30000) // 30 seconds
            }
        }
        
        // Cooldown can be faster
        viewModelScope.launch {
            while (true) {
                fetchCooldown()
                kotlinx.coroutines.delay(10000) // 10 seconds
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }

    fun fetchHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getMarketSummary()
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(summaries = list, isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message, isLoading = false)
                }
        }
    }

    fun fetchCooldown() {
        viewModelScope.launch {
            repository.getCooldownStatus()
                .onSuccess { status ->
                    _uiState.value = _uiState.value.copy(cooldownStatus = status)
                }
        }
    }

    fun triggerScan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(scanLoading = true, scanResult = null)
            repository.triggerFullScan()
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        scanLoading = false,
                        scanResult = response.message
                    )
                    fetchHomeData()
                    fetchCooldown()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        scanLoading = false,
                        scanResult = "Hata: ${error.message}"
                    )
                }
        }
    }
}
