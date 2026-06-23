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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.randomimage.domain.model.ImageModel
import com.randomimage.ui.viewmodel.FavoritesViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@OptIn(ExperimentalFoundationApi::class)
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
    val colorScheme = MiuixTheme.colorScheme

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
        // 分组标签
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            groups.forEachIndexed { index, groupName ->
                Button(
                    onClick = { selectedGroupIndex = index },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(groupName, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { showAddGroupDialog = true }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Add,
                    contentDescription = "添加分组",
                    tint = colorScheme.onBackground
                )
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
                        Text("还没有收藏", fontSize = 20.sp)
                        Text(
                            text = "在首页点击收藏按钮即可添加",
                            fontSize = 14.sp,
                            color = colorScheme.onSurface
                        )
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredFavorites, key = { it.id }) { image ->
                            Card(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .combinedClickable(
                                        onClick = { onImageClick(image) },
                                        onLongClick = { selectedImage = image }
                                    ),
                                showIndication = true
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

    // 新建分组对话框
    if (showAddGroupDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddGroupDialog = false },
            title = { Text("新建分组") },
            text = {
                TextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    label = "分组名称"
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newGroupName.isNotBlank()) {
                        viewModel.addGroup(newGroupName)
                        newGroupName = ""
                        showAddGroupDialog = false
                    }
                }) { Text("创建") }
            },
            dismissButton = {
                Button(onClick = { showAddGroupDialog = false; newGroupName = "" }) { Text("取消") }
            }
        )
    }

    // 收藏操作对话框
    selectedImage?.let { image ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { selectedImage = null },
            title = { Text("收藏操作") },
            text = {
                Column {
                    if (uiState.groups.isNotEmpty()) {
                        Text("移动到分组：", fontSize = 12.sp, color = colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.groups.forEach { group ->
                            SuperArrow(
                                title = group.name,
                                summary = "",
                                onClick = {
                                    viewModel.moveImageToGroup(image.id, group.id)
                                    selectedImage = null
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.removeFavorite(image.id)
                    selectedImage = null
                }) { Text("删除") }
            },
            dismissButton = {
                Button(onClick = { selectedImage = null }) { Text("取消") }
            }
        )
    }
}
