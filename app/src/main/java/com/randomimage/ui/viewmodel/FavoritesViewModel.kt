package com.randomimage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.randomimage.data.repository.ImageRepository
import com.randomimage.domain.model.ImageModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favorites: List<ImageModel> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getFavorites().collect { favorites ->
                _uiState.value = _uiState.value.copy(
                    favorites = favorites,
                    isLoading = false
                )
            }
        }
    }

    fun removeFavorite(imageId: String) {
        viewModelScope.launch {
            repository.removeFromFavorites(imageId)
        }
    }
}
