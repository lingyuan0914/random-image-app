package com.randomimage.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.randomimage.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WaterfallScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onImageClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val gridState = rememberLazyStaggeredGridState()
    var lastLoadTime by remember { mutableStateOf(0L) }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = gridState.layoutInfo.totalItemsCount
            val currentTime = System.currentTimeMillis()
            // 只有向下滚动到底部时才加载更多
            lastVisibleItem >= totalItems - 4 && totalItems > 0 && currentTime - lastLoadTime > 2000
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && uiState.images.isNotEmpty() && !uiState.isLoading) {
            lastLoadTime = System.currentTimeMillis()
            viewModel.loadMoreImages()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = uiState.currentApiName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("数据源", style = MaterialTheme.typography.labelSmall) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .height(56.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    uiState.availableApis.forEachIndexed { index, apiName ->
                        DropdownMenuItem(
                            text = { Text(apiName) },
                            onClick = {
                                viewModel.switchApi(index)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "NSFW",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = uiState.isNSFW,
                    onCheckedChange = { viewModel.toggleNSFW() }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("搜索标签", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("输入关键词...", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (searchQuery.isNotBlank()) {
                        viewModel.searchImages(searchQuery)
                    }
                }
            ) {
                Icon(Icons.Default.Search, contentDescription = "搜索")
            }
        }

        if (uiState.recommendedTags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .height(36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "推荐:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    uiState.recommendedTags.take(6).forEach { tag ->
                        SuggestionChip(
                            onClick = {
                                searchQuery = tag.name
                                viewModel.searchImages(tag.name)
                            },
                            label = { Text(tag.displayName, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading && uiState.images.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null && uiState.images.isEmpty() -> {
                    androidx.compose.material3.TextButton(
                        onClick = { viewModel.loadImages() },
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text(
                            text = uiState.error ?: "错误",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                uiState.images.isNotEmpty() -> {
                    val columns = when {
                        screenWidth < 400.dp -> 2
                        screenWidth < 600.dp -> 3
                        else -> 4
                    }

                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(columns),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalItemSpacing = 6.dp
                    ) {
                        items(
                            items = uiState.images,
                            key = { it.id }
                        ) { image ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val globalIndex = uiState.images.indexOf(image)
                                        onImageClick(globalIndex)
                                    },
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(image.urls.regular)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = image.description,
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
