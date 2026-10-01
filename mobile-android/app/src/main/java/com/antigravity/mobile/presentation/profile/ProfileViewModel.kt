package com.antigravity.mobile.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.mobile.data.config.SupabaseConfig
import com.antigravity.mobile.domain.model.Portfolio
import com.antigravity.mobile.domain.model.User
import com.antigravity.mobile.domain.repository.AuthRepository
import com.antigravity.mobile.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val portfolio: Portfolio? = null,
    val tradeCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedOut: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository,
    private val config: SupabaseConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(config.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        val loggedIn = config.isLoggedIn()
        _isLoggedIn.value = loggedIn
        if (!loggedIn) {
            _uiState.value = ProfileUiState(
                user = null,
                portfolio = null,
                tradeCount = 0,
                isLoading = false
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val user = authRepository.getCurrentUser()
            val portfolioResult = gameRepository.getPortfolio()
            val historyResult = gameRepository.getTradeHistory()

            _uiState.value = _uiState.value.copy(
                user = user,
                portfolio = portfolioResult.getOrNull(),
                tradeCount = historyResult.getOrNull()?.size ?: 0,
                isLoading = false
            )
        }
    }

    fun refresh() {
        loadProfileData()
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _isLoggedIn.value = false
            _uiState.value = _uiState.value.copy(
                user = null,
                portfolio = null,
                tradeCount = 0,
                isLoggedOut = true
            )
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepository.deleteAccount()
                .onSuccess {
                    _isLoggedIn.value = false
                    _uiState.value = _uiState.value.copy(isDeleted = true, isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message, isLoading = false)
                }
        }
    }
}
