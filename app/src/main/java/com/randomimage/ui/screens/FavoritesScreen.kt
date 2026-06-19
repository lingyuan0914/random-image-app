package com.randomimage.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.randomimage.domain.model.ImageModel
import com.randomimage.ui.viewmodel.FavoritesViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onImageClick: (ImageModel) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedImage by remember { mutableStateOf<ImageModel?>(null) }
    var selectedGroupIndex by remember { mutableIntStateOf(0) }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }

    val groups = listOf("全部") + uiState.groups.map { it.name }
    val filteredFavorites = if (selectedGroupIndex == 0) {
        uiState.favorites
    } else {
        val group = uiState.groups.getOrNull(selectedGroupIndex - 1)
        if (group != null) {
            uiState.favorites.filter { it.groupId.toString() == group.id }
        } else {
            uiState.favorites
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedGroupIndex,
                modifier = Modifier.weight(1f),
                edgePadding = 8.dp
            ) {
                groups.forEachIndexed { index, groupName ->
                    Tab(
                        selected = selectedGroupIndex == index,
                        onClick = { selectedGroupIndex = index },
                        text = { Text(groupName) }
                    )
                }
            }
            IconButton(onClick = { showAddGroupDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "添加分组")
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                filteredFavorites.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("还没有收藏", style = MaterialTheme.typography.headlineSmall)
                        Text("在首页点击收藏按钮即可添加", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredFavorites) { image ->
                            Card(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .combinedClickable(
                                        onClick = { onImageClick(image) },
                                        onLongClick = { selectedImage = image }
                                    ),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                AsyncImage(
                                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                                        .data(image.localPath ?: image.urls.thumb)
                                        .memoryCacheKey("fav_${image.id}")
                                        .crossfade(false)
                                        .allowHardware(true)
                                        .build(),
                                    contentDescription = image.description,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddGroupDialog) {
        AlertDialog(
            onDismissRequest = { showAddGroupDialog = false },
            title = { Text("新建分组") },
            text = {
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    label = { Text("分组名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newGroupName.isNotBlank()) {
                        viewModel.addGroup(newGroupName)
                        newGroupName = ""
                        showAddGroupDialog = false
                    }
                }) { Text("创建") }
            },
            dismissButton = {
                TextButton(onClick = { showAddGroupDialog = false; newGroupName = "" }) { Text("取消") }
            }
        )
    }

    selectedImage?.let { image ->
        AlertDialog(
            onDismissRequest = { selectedImage = null },
            title = { Text("收藏操作") },
            text = {
                Column {
                    Text("选择操作：")
                    Spacer(modifier = Modifier.height(8.dp))
                    if (uiState.groups.isNotEmpty()) {
                        Text("移动到分组：", style = MaterialTheme.typography.bodySmall)
                        uiState.groups.forEach { group ->
                            TextButton(
                                onClick = {
                                    viewModel.moveImageToGroup(image.id, group.id)
                                    selectedImage = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(group.name)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeFavorite(image.id)
                    selectedImage = null
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { selectedImage = null }) { Text("取消") }
            }
        )
    }
}
