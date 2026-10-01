package com.antigravity.mobile.presentation.signals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.mobile.domain.usecase.GetActiveSignalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignalsViewModel @Inject constructor(
    private val getActiveSignalsUseCase: GetActiveSignalsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignalsUiState>(SignalsUiState.Loading)
    val uiState: StateFlow<SignalsUiState> = _uiState.asStateFlow()

    init {
        loadSignals()
    }

    fun loadSignals() {
        viewModelScope.launch {
            _uiState.value = SignalsUiState.Loading
            getActiveSignalsUseCase().collect { result ->
                result.onSuccess { signals ->
                    _uiState.value = SignalsUiState.Success(signals)
                }.onFailure { throwable ->
                    _uiState.value = SignalsUiState.Error(
                        throwable.localizedMessage ?: "Sinyaller yüklenemedi."
                    )
                }
            }
        }
    }
}
