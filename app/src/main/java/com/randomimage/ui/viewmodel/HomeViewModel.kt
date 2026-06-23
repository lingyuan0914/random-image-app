package com.randomimage.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import coil.ImageLoader
import com.randomimage.data.remote.ApiManager
import com.randomimage.data.remote.ImagePagingSource
import com.randomimage.data.repository.ImageRepository
import com.randomimage.domain.model.ImageModel
import com.randomimage.util.StatsManager
import com.randomimage.util.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    val availableApis: List<String> = emptyList(),
    val isNSFW: Boolean = false,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val recommendedTags: List<RecommendedTag> = emptyList(),
    val favorites: List<ImageModel> = emptyList(),
    val history: List<ImageModel> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isFollowingArtist: Boolean = false,
    val detailImage: ImageModel? = null,
    val expandImageBounds: androidx.compose.ui.geometry.Rect? = null,
    val imageQuality: ImageQuality = ImageQuality.MEDIUM,
    val popularTags: List<com.randomimage.data.local.TagData> = emptyList(),
    val memoryImages: List<ImageModel> = emptyList()
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
    private val imageLoader: coil.ImageLoader
) : AndroidViewModel(application) {

    private val loadMoreMutex = Mutex()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _pagingImages = MutableStateFlow<PagingData<ImageModel>>(PagingData.empty())
    val pagingImages: StateFlow<PagingData<ImageModel>> = _pagingImages.asStateFlow()

    private val favoritesFlow = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val historyFlow = repository.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val recentSearchesFlow = repository.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val favoriteIdsCache = mutableSetOf<String>()
    private val followingCache = mutableSetOf<String>()
    private val artistsFlow = repository.getFollowedArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        StatsManager.updateFirstOpenTime(getApplication())

        _uiState.value = _uiState.value.copy(
            availableApis = apiManager.availableApis.map { it.name },
            recommendedTags = emptyList()
        )

        viewModelScope.launch {
            favoritesFlow.collect { favorites ->
                favoriteIdsCache.clear()
                favoriteIdsCache.addAll(favorites.map { it.id })
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
        viewModelScope.launch {
            artistsFlow.collect { artists ->
                followingCache.clear()
                followingCache.addAll(artists.map { it.uid })
            }
        }

        loadMemoryImages()
        loadPagingImages()
    }

    private fun loadMemoryImages() {
        viewModelScope.launch {
            val hist = repository.getHistory().first()
            val cal = java.util.Calendar.getInstance()
            val month = cal.get(java.util.Calendar.MONTH)
            val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
            val yearAgo = System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000
            val monthAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            val memoryImages = hist.filter { image ->
                val viewedAt = image.viewedAt
                when {
                    viewedAt in yearAgo..yearAgo + 2L * 24 * 60 * 60 * 1000 -> {
                        val viewedCal = java.util.Calendar.getInstance().apply { timeInMillis = viewedAt }
                        viewedCal.get(java.util.Calendar.MONTH) == month &&
                        viewedCal.get(java.util.Calendar.DAY_OF_MONTH) == day
                    }
                    viewedAt in monthAgo..System.currentTimeMillis() -> true
                    else -> false
                }
            }.take(5)
            _uiState.value = _uiState.value.copy(memoryImages = memoryImages)
        }
    }

    fun loadPagingImages() {
        viewModelScope.launch {
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 10,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    ImagePagingSource(
                        apiManager = apiManager,
                        isNSFW = _uiState.value.isNSFW
                    )
                }
            ).flow.cachedIn(viewModelScope).collect { pagingData ->
                _pagingImages.value = pagingData
            }
        }
        _uiState.value = _uiState.value.copy(currentApiName = apiManager.currentApi.name)
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
                images.forEach { image ->
                    image.tags.forEach { tag ->
                        repository.recordTagUsage(tag)
                    }
                }
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

    fun searchPagingImages(query: String) {
        if (query.isBlank()) {
            loadPagingImages()
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, searchQuery = query)
            repository.addSearchHistory(query)
            StatsManager.incrementSearchCount(getApplication())
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 10,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    ImagePagingSource(
                        apiManager = apiManager,
                        searchQuery = query
                    )
                }
            ).flow.cachedIn(viewModelScope).collect { pagingData ->
                _pagingImages.value = pagingData
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
        _uiState.value = _uiState.value.copy(
            currentApiName = apiManager.currentApi.name,
            recommendedTags = emptyList(),
            images = emptyList(),
            currentIndex = 0,
            isLoading = true,
            error = null
        )
        loadPagingImages()
    }

    fun refreshApis() {
        apiManager.refreshCustomApis()
        _uiState.value = _uiState.value.copy(
            availableApis = apiManager.availableApis.map { it.name }
        )
    }

    fun toggleNSFW() {
        val newState = !_uiState.value.isNSFW
        _uiState.value = _uiState.value.copy(isNSFW = newState)
        Timber.d("NSFW toggled to $newState")
        loadPagingImages()
    }

    private fun nextImage() {
        val state = _uiState.value
        if (state.currentIndex < state.images.size - 1) {
            _uiState.value = state.copy(currentIndex = state.currentIndex + 1)
            checkFavorite()
            loadMoreIfNeeded()
        } else {
            loadPopularTags()
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
            imageLoader.enqueue(request)
        }
    }

    fun setCurrentIndex(index: Int) {
        _uiState.value = _uiState.value.copy(currentIndex = index, detailImage = null)
        checkFavorite()
    }

    fun setDetailImage(image: ImageModel, bounds: androidx.compose.ui.geometry.Rect? = null) {
        _uiState.value = _uiState.value.copy(detailImage = image, expandImageBounds = bounds)
        checkFavorite()
    }

    fun setExpandBounds(bounds: FloatArray) {
        val rect = androidx.compose.ui.geometry.Rect(bounds[0], bounds[1], bounds[2], bounds[3])
        _uiState.value = _uiState.value.copy(expandImageBounds = rect)
    }

    fun clearExpandBounds() {
        _uiState.value = _uiState.value.copy(expandImageBounds = null)
    }

    private fun checkFavorite() {
        viewModelScope.launch {
            val currentImage = _uiState.value.detailImage ?: getCurrentImage()
            if (currentImage != null) {
                val isFavorite = currentImage.id in favoriteIdsCache
                _uiState.value = _uiState.value.copy(isFavorite = isFavorite)
            }
        }
    }

    fun toggleFavorite() {
        val currentImage = _uiState.value.detailImage ?: getCurrentImage() ?: return
        viewModelScope.launch {
            if (_uiState.value.isFavorite) {
                repository.removeFromFavorites(currentImage.id)
                favoriteIdsCache.remove(currentImage.id)
                _uiState.value = _uiState.value.copy(isFavorite = false)
                Timber.d("Removed from favorites: ${currentImage.id}")
            } else {
                repository.addToFavorites(currentImage)
                favoriteIdsCache.add(currentImage.id)
                _uiState.value = _uiState.value.copy(isFavorite = true)
                StatsManager.incrementFavoriteCount(getApplication())
                Timber.d("Added to favorites: ${currentImage.id}")
            }
        }
    }

    fun addToFavoritesWithGroup(image: ImageModel, groupId: String) {
        viewModelScope.launch {
            repository.addToFavoritesWithGroup(image, groupId)
            favoriteIdsCache.add(image.id)
            _uiState.value = _uiState.value.copy(isFavorite = true)
            StatsManager.incrementFavoriteCount(getApplication())
            Timber.d("Added to favorites with group $groupId: ${image.id}")
        }
    }

    suspend fun getGroups(): List<com.randomimage.data.local.GroupData> {
        return repository.getGroups().first()
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
                    currentImage.urls.raw
                )
                // Note: StatsManager.incrementDownloadCount is called inside DownloadManager
                _uiState.value = _uiState.value.copy(isDownloading = false)
                Timber.d("Download ${if (success) "success" else "failed"}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isDownloading = false)
                Timber.e(e, "Download failed")
            }
        }
    }

    fun swipeToNext() {
        val image = getCurrentImage() ?: return
        viewModelScope.launch {
            repository.addToHistory(image)
        }
        val state = _uiState.value
        if (state.currentIndex < state.images.size - 1) {
            _uiState.value = state.copy(currentIndex = state.currentIndex + 1)
            checkFavorite()
        }
    }

    fun swipeToPrev() {
        val image = getCurrentImage() ?: return
        viewModelScope.launch {
            repository.addToHistory(image)
        }
        val state = _uiState.value
        if (state.currentIndex > 0) {
            _uiState.value = state.copy(currentIndex = state.currentIndex - 1)
            checkFavorite()
        }
    }

    fun getCurrentImage(): ImageModel? {
        val state = _uiState.value
        return state.images.getOrNull(state.currentIndex)
    }

    fun toggleTheme(context: Context) {
        val currentMode = ThemeManager.getColorMode(context)
        val newMode = if (currentMode.isDark) {
            com.randomimage.ui.theme.ColorMode.LIGHT
        } else {
            com.randomimage.ui.theme.ColorMode.DARK
        }
        ThemeManager.setColorMode(context, newMode)
        Timber.d("Theme toggled to ${if (newMode.isDark) "dark" else "light"}")
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

    fun toggleFollowArtist() {
        val image = getCurrentImage() ?: return
        viewModelScope.launch {
            if (_uiState.value.isFollowingArtist) {
                repository.unfollowArtist(image.user.id)
                followingCache.remove(image.user.id)
                _uiState.value = _uiState.value.copy(isFollowingArtist = false)
                Timber.d("Unfollowed artist: ${image.user.name}")
            } else {
                repository.followArtist(
                    com.randomimage.data.local.ArtistData(
                        uid = image.user.id,
                        name = image.user.name
                    )
                )
                followingCache.add(image.user.id)
                _uiState.value = _uiState.value.copy(isFollowingArtist = true)
                Timber.d("Followed artist: ${image.user.name}")
            }
        }
    }

    fun checkFollowingArtist() {
        val image = getCurrentImage() ?: return
        viewModelScope.launch {
            val following = image.user.id in followingCache
            _uiState.value = _uiState.value.copy(isFollowingArtist = following)
        }
    }

    private var tagsLoaded = false

    fun loadPopularTags() {
        if (tagsLoaded) return
        viewModelScope.launch {
            val tags = repository.getAllTags().first()
            _uiState.value = _uiState.value.copy(popularTags = tags)
            tagsLoaded = true
        }
    }

    fun recordCurrentImageTags() {
        val image = getCurrentImage() ?: return
        viewModelScope.launch {
            image.tags.forEach { tag ->
                repository.recordTagUsage(tag)
            }
        }
    }
}
