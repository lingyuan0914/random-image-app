package com.randomimage.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.randomimage.ui.viewmodel.HomeViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun WaterfallScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onImageClick: (com.randomimage.domain.model.ImageModel) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagingImages = viewModel.pagingImages.collectAsLazyPagingItems()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    var showNsfwDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val gridState = rememberLazyStaggeredGridState()
    val colorScheme = MiuixTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize()) {
        // 数据源选择 + NSFW 开关
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // API 下拉选择 - 使用 Material3 ExposedDropdownMenu
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = uiState.currentApiName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("数据源") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
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
                    fontSize = 12.sp,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = uiState.isNSFW,
                    onCheckedChange = { showNsfwDialog = true }
                )
            }
        }

        // 搜索框
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = "搜索标签",
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (uiState.searchQuery.isNotBlank()) {
                        viewModel.searchPagingImages(uiState.searchQuery)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = colorScheme.onBackground
                )
            }
        }

        // 历史搜索标签
        if (uiState.recentSearches.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .height(36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "历史:",
                    fontSize = 12.sp,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    uiState.recentSearches.take(6).forEach { query ->
                        Button(
                            onClick = { viewModel.setSearchQuery(query); viewModel.searchPagingImages(query) },
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(query, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 推荐标签
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
                    fontSize = 12.sp,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    uiState.recommendedTags.take(6).forEach { tag ->
                        Button(
                            onClick = { viewModel.setSearchQuery(tag.name); viewModel.searchPagingImages(tag.name) },
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(tag.displayName, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 图片网格
        Box(modifier = Modifier.weight(1f)) {
            when {
                pagingImages.loadState.refresh is LoadState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                pagingImages.loadState.refresh is LoadState.Error -> {
                    val error = (pagingImages.loadState.refresh as LoadState.Error).error
                    Button(
                        onClick = { pagingImages.retry() },
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text(error.message ?: "错误，点击重试")
                    }
                }
                pagingImages.itemCount > 0 -> {
                    val columns = when {
                        configuration.screenWidthDp < 400 -> 2
                        configuration.screenWidthDp < 600 -> 3
                        else -> 4
                    }

                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(columns),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalItemSpacing = 3.dp
                    ) {
                        items(
                            count = pagingImages.itemCount,
                            key = { index ->
                                val item = pagingImages.peek(index)
                                "${item?.id}_$index"
                            }
                        ) { index ->
                            val image = pagingImages[index] ?: return@items
                            var itemBounds by remember { mutableStateOf(Rect.Zero) }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onGloballyPositioned { coordinates ->
                                        val pos = coordinates.positionInRoot()
                                        val size = coordinates.size
                                        itemBounds = Rect(
                                            pos.x, pos.y, pos.x + size.width, pos.y + size.height
                                        )
                                    }
                                    .clickable {
                                        viewModel.setExpandBounds(
                                            floatArrayOf(itemBounds.left, itemBounds.top, itemBounds.right, itemBounds.bottom)
                                        )
                                        onImageClick(image)
                                    },
                                showIndication = true,
                                pressFeedbackType = PressFeedbackType.Sink
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(image.localPath ?: image.urls.thumb)
                                        .memoryCacheKey(image.id)
                                        .crossfade(false)
                                        .size(300, 400)
                                        .allowHardware(true)
                                        .build(),
                                    contentDescription = image.description,
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            }
                        }

                        if (pagingImages.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // NSFW 确认对话框
    if (showNsfwDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showNsfwDialog = false },
            title = { Text("NSFW 模式") },
            text = { Text("开启后将显示成人内容，确定要继续吗？") },
            confirmButton = {
                Button(onClick = { viewModel.toggleNSFW(); showNsfwDialog = false }) { Text("确定") }
            },
            dismissButton = {
                Button(onClick = { showNsfwDialog = false }) { Text("取消") }
            }
        )
    }
}
