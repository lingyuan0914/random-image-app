package com.randomimage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.randomimage.data.local.FavoriteGroupDao
import com.randomimage.data.local.FavoriteGroupEntity
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
    val groups: List<FavoriteGroupEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: ImageRepository,
    private val favoriteGroupDao: FavoriteGroupDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
        loadGroups()
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

    private fun loadGroups() {
        viewModelScope.launch {
            favoriteGroupDao.getAllGroups().collect { groups ->
                _uiState.value = _uiState.value.copy(groups = groups)
            }
        }
    }

    fun addGroup(name: String) {
        viewModelScope.launch {
            favoriteGroupDao.insertGroup(FavoriteGroupEntity(name = name))
        }
    }

    fun deleteGroup(group: FavoriteGroupEntity) {
        viewModelScope.launch {
            favoriteGroupDao.deleteGroup(group)
        }
    }

    fun removeFavorite(imageId: String) {
        viewModelScope.launch {
            repository.removeFromFavorites(imageId)
        }
    }
}
