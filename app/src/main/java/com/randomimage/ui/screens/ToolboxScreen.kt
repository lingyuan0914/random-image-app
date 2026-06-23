package com.randomimage.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text

data class ToolCategory(
    val name: String,
    val count: Int,
    val tools: List<ToolItem>
)

data class ToolItem(
    val name: String,
    val url: String? = null,
    val description: String = ""
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ToolboxScreen() {
    val context = LocalContext.current

    val categories = listOf(
        ToolCategory("图片类API", 7, listOf(
            ToolItem("Lolicon", "https://api.lolicon.app/setu/v2", "二次元图片API"),
            ToolItem("色图API", "https://sex.nyan.run/api/v2/", "二次元色图API"),
            ToolItem("Kori图库", "http://api.kori.moe/img", "随机图片API"),
            ToolItem("随机美图", "https://img.xjh.me/random_img.php", "随机美图API"),
            ToolItem("必应每日一图", "https://api.codelife.cc/api/top/bing", "必应每日壁纸"),
            ToolItem("B站封面", "https://api.vvhan.com/api/bilibili", "获取B站视频封面"),
            ToolItem("二次元风景", "https://t.mwm.moe/fj/", "二次元风景图片")
        )),
        ToolCategory("文字类API", 3, listOf(
            ToolItem("一言", "https://v1.hitokoto.cn", "随机一言API"),
            ToolItem("古诗词", "https://v2.jinrishici.com/one.json", "古诗词API"),
            ToolItem("彩虹屁", "https://api.03c3.cn/api/zb", "彩虹屁API")
        )),
        ToolCategory("工具类API", 4, listOf(
            ToolItem("IP查询", "https://api.vvhan.com/api/ip", "在线IP查询"),
            ToolItem("快递查询", "https://api.vvhan.com/api/kuaidi", "快递查询API"),
            ToolItem("二维码生成", "https://api.vvhan.com/api/ewm", "二维码生成"),
            ToolItem("短网址生成", "https://api.vvhan.com/api/dw", "短网址生成")
        ))
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "工具箱",
            fontSize = 20.sp,
            modifier = Modifier.padding(16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = category.name, fontSize = 16.sp)
                        Text(text = "${category.count} 个工具", fontSize = 12.sp)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            category.tools.forEach { tool ->
                                Card(
                                    modifier = Modifier
                                        .clickable {
                                            tool.url?.let { url ->
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                ) {
                                    Text(
                                        text = tool.name,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
