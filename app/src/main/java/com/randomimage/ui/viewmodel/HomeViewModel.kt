package com.randomimage.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.randomimage.data.remote.ApiManager
import com.randomimage.data.repository.ImageRepository
import com.randomimage.domain.model.ImageModel
import com.randomimage.util.StatsManager
import com.randomimage.util.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

data class RecommendedTag(
    val name: String,
    val displayName: String
)

data class HomeUiState(
    val images: List<ImageModel> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val error: String? = null,
    val currentApiName: String = "Lolicon",
    val availableApis: List<String> = listOf("Lolicon", "萌图", "色图API", "Kori图库", "随机美图", "二次元风景"),
    val isNSFW: Boolean = false,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val recommendedTags: List<RecommendedTag> = emptyList(),
    val favorites: List<ImageModel> = emptyList(),
    val history: List<ImageModel> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val showDetail: Boolean = false,
    val isWaterfall: Boolean = false,
    val imageQuality: ImageQuality = ImageQuality.MEDIUM
)

enum class ImageQuality(val label: String, val size: Int) {
    THUMBNAIL("缩略图", 200),
    MEDIUM("中等", 400),
    ORIGINAL("原图", 0)
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val apiManager: ApiManager,
    private val repository: ImageRepository,
    private val favoriteGroupDao: com.randomimage.data.local.FavoriteGroupDao
) : AndroidViewModel(application) {

    private val loadMoreMutex = Mutex()

    private val loliconTags = listOf(
        RecommendedTag("白丝", "白丝"),
        RecommendedTag("黑丝", "黑丝"),
        RecommendedTag("泳装", "泳装"),
        RecommendedTag("比基尼", "比基尼"),
        RecommendedTag("学校泳装", "学校泳装"),
        RecommendedTag("女仆", "女仆"),
        RecommendedTag("兔女郎", "兔女郎"),
        RecommendedTag("原神", "原神"),
        RecommendedTag("崩坏", "崩坏"),
        RecommendedTag("Fate", "Fate"),
        RecommendedTag("碧蓝航线", "碧蓝航线"),
        RecommendedTag("舰队", "舰队Collection")
    )

    private val koriTags = listOf(
        RecommendedTag("default", "默认"),
        RecommendedTag("anime", "动漫"),
        RecommendedTag("landscape", "风景"),
        RecommendedTag("girl", "少女"),
        RecommendedTag("R18", "R18")
    )

    private val sexPhotoTags = listOf(
        RecommendedTag("碧蓝航线", "碧蓝航线"),
        RecommendedTag("原神", "原神"),
        RecommendedTag("Fate", "Fate"),
        RecommendedTag("东方", "东方"),
        RecommendedTag("舰队Collection", "舰队Collection"),
        RecommendedTag("碧蓝档案", "碧蓝档案"),
        RecommendedTag("少女前线", "少女前线"),
        RecommendedTag("hololive", "hololive"),
        RecommendedTag("初音未来", "初音未来"),
        RecommendedTag("loli", "萝莉"),
        RecommendedTag("泳装", "泳装"),
        RecommendedTag("白丝", "白丝")
    )

    private val moeImgTags = listOf(
        RecommendedTag("风景", "风景"),
        RecommendedTag("动漫", "动漫"),
        RecommendedTag("可爱", "可爱"),
        RecommendedTag("唯美", "唯美")
    )

    private val xjhTags = listOf(
        RecommendedTag("随机", "随机美图"),
        RecommendedTag("二次元", "二次元"),
        RecommendedTag("动漫", "动漫")
    )

    private val mwmTags = listOf(
        RecommendedTag("风景", "二次元风景"),
        RecommendedTag("壁纸", "壁纸"),
        RecommendedTag("唯美", "唯美")
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val favoritesFlow = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val historyFlow = repository.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val recentSearchesFlow = repository.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        StatsManager.updateFirstOpenTime(getApplication())

        _uiState.value = _uiState.value.copy(
            availableApis = apiManager.availableApis.map { it.name },
            recommendedTags = loliconTags
        )

        viewModelScope.launch {
            favoritesFlow.collect { favorites ->
                _uiState.value = _uiState.value.copy(favorites = favorites)
            }
        }
        viewModelScope.launch {
            historyFlow.collect { history ->
                _uiState.value = _uiState.value.copy(history = history)
            }
        }
        viewModelScope.launch {
            recentSearchesFlow.collect { searches ->
                _uiState.value = _uiState.value.copy(recentSearches = searches)
            }
        }

        loadImages()
    }

    fun loadImages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val images = if (_uiState.value.isNSFW) {
                    repository.fetchRandomImagesNSFW(20)
                } else {
                    repository.fetchRandomImages(20)
                }
                _uiState.value = _uiState.value.copy(
                    images = images,
                    currentIndex = 0,
                    isLoading = false,
                    currentApiName = apiManager.currentApi.name
                )
                StatsManager.incrementViewCount(getApplication())
                Timber.d("Loaded ${images.size} images")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load images")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "未知错误"
                )
            }
        }
    }

    fun loadMoreImages() {
        viewModelScope.launch {
            loadMoreMutex.withLock {
                if (_uiState.value.isLoading) return@withLock
                try {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    val newImages = if (_uiState.value.isNSFW) {
                        repository.fetchRandomImagesNSFW(10)
                    } else {
                        repository.fetchRandomImages(10)
                    }
                    val existingIds = _uiState.value.images.map { it.id }.toSet()
                    val uniqueNewImages = newImages.filter { it.id !in existingIds }
                    if (uniqueNewImages.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            images = _uiState.value.images + uniqueNewImages,
                            isLoading = false
                        )
                        Timber.d("Loaded ${uniqueNewImages.size} more images")
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to load more images")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun searchImages(query: String) {
        if (query.isBlank()) {
            loadImages()
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSearching = true, searchQuery = query)
            try {
                repository.addSearchHistory(query)
                StatsManager.incrementSearchCount(getApplication())
                val images = repository.searchImages(query)
                _uiState.value = _uiState.value.copy(
                    images = images,
                    currentIndex = 0,
                    isLoading = false
                )
                Timber.d("Search '$query' returned ${images.size} images")
            } catch (e: Exception) {
                Timber.e(e, "Search failed for '$query'")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "搜索失败"
                )
            }
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "", isSearching = false)
        loadImages()
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun switchApi(index: Int) {
        apiManager.switchApi(index)
        val tags = when (apiManager.currentApi.name) {
            "Lolicon" -> loliconTags
            "萌图" -> moeImgTags
            "色图API" -> sexPhotoTags
            "Kori图库" -> koriTags
            "随机美图" -> xjhTags
            "二次元风景" -> mwmTags
            else -> emptyList()
        }
        _uiState.value = _uiState.value.copy(
            currentApiName = apiManager.currentApi.name,
            recommendedTags = tags
        )
        loadImages()
    }

    fun toggleNSFW() {
        val newState = !_uiState.value.isNSFW
        _uiState.value = _uiState.value.copy(isNSFW = newState)
        Timber.d("NSFW toggled to $newState")
        loadImages()
    }

    fun swipeRight() {
        val currentImage = getCurrentImage() ?: return
        viewModelScope.launch {
            repository.addToHistory(currentImage)
            nextImage()
        }
    }

    fun swipeLeft() {
        val currentImage = getCurrentImage() ?: return
        viewModelScope.launch {
            repository.addToHistory(currentImage)
            nextImage()
        }
    }

    private fun nextImage() {
        val state = _uiState.value
        if (state.currentIndex < state.images.size - 1) {
            _uiState.value = state.copy(currentIndex = state.currentIndex + 1)
            checkFavorite()
            loadMoreIfNeeded()
        } else {
            loadImages()
        }
    }

    private fun loadMoreIfNeeded() {
        val state = _uiState.value
        if (state.currentIndex >= state.images.size - 5) {
            loadMoreImages()
        }
        preloadNextImages()
    }

    private fun preloadNextImages() {
        val state = _uiState.value
        val nextIndices = listOf(
            state.currentIndex + 1,
            state.currentIndex + 2,
            state.currentIndex + 3
        ).filter { it < state.images.size }

        nextIndices.forEach { index ->
            val image = state.images[index]
            val request = coil.request.ImageRequest.Builder(getApplication())
                .data(image.urls.regular)
                .memoryCacheKey("preload_${image.id}")
                .build()
            coil.ImageLoader(getApplication()).enqueue(request)
        }
    }

    fun setCurrentIndex(index: Int) {
        _uiState.value = _uiState.value.copy(currentIndex = index)
        checkFavorite()
    }

    fun setShowDetail(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDetail = show)
    }

    fun setIsWaterfall(waterfall: Boolean) {
        _uiState.value = _uiState.value.copy(isWaterfall = waterfall)
    }

    private fun checkFavorite() {
        viewModelScope.launch {
            val currentImage = getCurrentImage()
            if (currentImage != null) {
                val isFavorite = repository.isFavorite(currentImage.id)
                _uiState.value = _uiState.value.copy(isFavorite = isFavorite)
            }
        }
    }

    fun toggleFavorite() {
        val currentImage = getCurrentImage() ?: return
        viewModelScope.launch {
            if (_uiState.value.isFavorite) {
                repository.removeFromFavorites(currentImage.id)
                _uiState.value = _uiState.value.copy(isFavorite = false)
                Timber.d("Removed from favorites: ${currentImage.id}")
            } else {
                repository.addToFavorites(currentImage)
                _uiState.value = _uiState.value.copy(isFavorite = true)
                StatsManager.incrementFavoriteCount(getApplication())
                Timber.d("Added to favorites: ${currentImage.id}")
            }
        }
    }

    fun addToFavoritesWithGroup(image: ImageModel, groupId: Long) {
        viewModelScope.launch {
            repository.addToFavoritesWithGroup(image, groupId)
            _uiState.value = _uiState.value.copy(isFavorite = true)
            StatsManager.incrementFavoriteCount(getApplication())
            Timber.d("Added to favorites with group $groupId: ${image.id}")
        }
    }

    suspend fun getGroups(): List<com.randomimage.data.local.FavoriteGroupEntity> {
        return favoriteGroupDao.getAllGroupsSync()
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            Timber.d("History cleared")
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
            Timber.d("Search history cleared")
        }
    }

    fun clearCache() {
        val context = getApplication<Application>()
        val cacheDir = context.cacheDir.resolve("image_cache")
        cacheDir.deleteRecursively()
        Timber.d("Cache cleared")
    }

    fun downloadCurrentImage() {
        val currentImage = getCurrentImage() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDownloading = true)
            try {
                val success = com.randomimage.util.ImageUtils.downloadImage(
                    getApplication(),
                    currentImage.urls.regular
                )
                if (success) {
                    StatsManager.incrementDownloadCount(getApplication())
                }
                _uiState.value = _uiState.value.copy(isDownloading = false)
                Timber.d("Download ${if (success) "success" else "failed"}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isDownloading = false)
                Timber.e(e, "Download failed")
            }
        }
    }

    fun getCurrentImage(): ImageModel? {
        val state = _uiState.value
        return state.images.getOrNull(state.currentIndex)
    }

    fun toggleTheme(context: Context) {
        val currentMode = ThemeManager.getThemeMode(context)
        val newMode = if (currentMode == ThemeManager.THEME_DARK) {
            ThemeManager.THEME_LIGHT
        } else {
            ThemeManager.THEME_DARK
        }
        ThemeManager.setThemeMode(context, newMode)
        Timber.d("Theme toggled to ${if (newMode == ThemeManager.THEME_DARK) "dark" else "light"}")
    }

    fun setImageQuality(quality: ImageQuality) {
        _uiState.value = _uiState.value.copy(imageQuality = quality)
        Timber.d("Image quality set to ${quality.label}")
    }

    fun setCurrentImage(image: ImageModel) {
        val index = _uiState.value.images.indexOfFirst { it.id == image.id }
        if (index >= 0) {
            _uiState.value = _uiState.value.copy(currentIndex = index)
        } else {
            val newImages = _uiState.value.images.toMutableList()
            newImages.add(0, image)
            _uiState.value = _uiState.value.copy(images = newImages, currentIndex = 0)
        }
    }
}
