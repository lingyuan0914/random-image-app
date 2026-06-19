package com.randomimage.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.randomimage.util.ImageUtils
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropScreen(
    imageUrl: String,
    onBack: () -> Unit,
    onCropped: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var aspectRatio by remember { mutableFloatStateOf(1f) }

    PredictiveBackHandler { backEvents ->
        backEvents.collect { }
        onBack()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("裁剪图片") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(onClick = {
                    scope.launch {
                        val success = ImageUtils.downloadImage(context, imageUrl)
                        if (success) {
                            onCropped(imageUrl)
                            Toast.makeText(context, "图片已保存", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Icon(Icons.Default.Check, contentDescription = "保存")
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(Color.Black)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(false)
                    .size(coil.size.Size.ORIGINAL)
                    .build(),
                contentDescription = "裁剪预览",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 3f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    }
            )

            // 裁剪框
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val boxSize = size.width * 0.8f
                val boxOffset = Offset(
                    (size.width - boxSize) / 2,
                    (size.height - boxSize * aspectRatio) / 2
                )

                // 绘制半透明遮罩
                drawRect(
                    color = Color.Black.copy(alpha = 0.5f),
                    topLeft = Offset.Zero,
                    size = androidx.compose.ui.geometry.Size(size.width, size.height)
                )

                // 清除裁剪区域
                drawRect(
                    color = Color.Transparent,
                    topLeft = boxOffset,
                    size = androidx.compose.ui.geometry.Size(boxSize, boxSize * aspectRatio),
                    blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                )

                // 绘制裁剪框边框
                drawRect(
                    color = Color.White,
                    topLeft = boxOffset,
                    size = androidx.compose.ui.geometry.Size(boxSize, boxSize * aspectRatio),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // 裁剪控制
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("裁剪比例", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("1:1", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = aspectRatio,
                    onValueChange = { aspectRatio = it },
                    valueRange = 0.5f..2f,
                    steps = 5,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("2:1", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
