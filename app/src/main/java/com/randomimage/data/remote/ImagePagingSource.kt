package com.randomimage.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.randomimage.domain.model.ImageModel
import timber.log.Timber

class ImagePagingSource(
    private val apiManager: ApiManager,
    private val isNSFW: Boolean = false,
    private val searchQuery: String? = null
) : PagingSource<Int, ImageModel>() {

    override fun getRefreshKey(state: PagingState<Int, ImageModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ImageModel> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        return try {
            val images = if (searchQuery.isNullOrBlank()) {
                if (isNSFW) {
                    apiManager.fetchRandomImagesNSFW(pageSize)
                } else {
                    apiManager.fetchRandomImages(pageSize)
                }
            } else {
                apiManager.searchImages(searchQuery, pageSize)
            }

            Timber.d("PagingSource load: page=$page, size=${images.size}")

            LoadResult.Page(
                data = images,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (images.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            Timber.e(e, "PagingSource load failed")
            LoadResult.Error(e)
        }
    }
}
