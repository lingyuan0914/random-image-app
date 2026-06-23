package com.randomimage.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

data class StatsItem(
    val label: String,
    val value: Int,
    val color: Color
)

@Composable
fun StatsChart(
    items: List<StatsItem>,
    modifier: Modifier = Modifier
) {
    val maxValue = items.maxOfOrNull { it.value } ?: 1
    val colorScheme = MiuixTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth()) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.label,
                    fontSize = 12.sp,
                    color = colorScheme.onSurface,
                    modifier = Modifier.width(60.dp)
                )
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    val barWidth = (item.value.toFloat() / maxValue) * size.width
                    drawRoundRect(
                        color = item.color,
                        topLeft = Offset.Zero,
                        size = Size(barWidth, size.height),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${item.value}",
                    fontSize = 12.sp,
                    color = colorScheme.onSurface,
                    modifier = Modifier.width(40.dp)
                )
            }
        }
    }
}
