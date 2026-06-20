package com.randomimage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.randomimage.data.remote.CustomApiConfig
import com.randomimage.data.remote.CustomApiManager
import com.randomimage.data.remote.ApiType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomApisUiState(
    val apis: List<CustomApiConfig> = emptyList(),
    val presetApis: List<com.randomimage.data.remote.PresetApi> = emptyList()
)

@HiltViewModel
class CustomApisViewModel @Inject constructor(
    private val customApiManager: CustomApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomApisUiState())
    val uiState: StateFlow<CustomApisUiState> = _uiState.asStateFlow()

    init {
        loadApis()
    }

    fun loadApis() {
        viewModelScope.launch {
            _uiState.value = CustomApisUiState(
                apis = customApiManager.getCustomApis(),
                presetApis = customApiManager.presetApis
            )
        }
    }

    fun addCustomApi(name: String, url: String, apiType: ApiType = ApiType.AUTO) {
        viewModelScope.launch {
            customApiManager.addCustomApi(name, url, apiType)
            loadApis()
        }
    }

    fun removeCustomApi(id: String) {
        viewModelScope.launch {
            customApiManager.removeCustomApi(id)
            loadApis()
        }
    }

    fun toggleCustomApi(id: String) {
        viewModelScope.launch {
            customApiManager.toggleCustomApi(id)
            loadApis()
        }
    }

    fun updateRateLimit(id: String, rateLimit: Int, rateLimitWindow: Int) {
        viewModelScope.launch {
            customApiManager.updateRateLimit(id, rateLimit, rateLimitWindow)
        }
    }
}
