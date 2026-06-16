package com.randomimage.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedQueue

object LogManager {
    private var logFile: File? = null
    private val logBuffer = ConcurrentLinkedQueue<String>()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun init(context: Context) {
        logFile = File(context.filesDir, "app_logs.txt")
        logFile?.createNewFile()
        addLog("INFO", "LogManager", "LogManager initialized")
    }

    fun addLog(level: String, tag: String, message: String) {
        val timestamp = LocalDateTime.now().format(formatter)
        val logEntry = "[$timestamp] $level/$tag: $message"
        logBuffer.add(logEntry)

        if (logBuffer.size >= 50) {
            flushLogs()
        }
    }

    fun flushLogs() {
        val entries = mutableListOf<String>()
        while (logBuffer.isNotEmpty()) {
            logBuffer.poll()?.let { entries.add(it) }
        }
        if (entries.isNotEmpty()) {
            logFile?.appendText(entries.joinToString("\n") + "\n")
        }
    }

    fun getLogs(): String {
        flushLogs()
        return try {
            logFile?.readText() ?: "No logs available"
        } catch (e: Exception) {
            "Error reading logs: ${e.message}"
        }
    }

    fun shareLogs(context: Context) {
        flushLogs()
        logFile?.let { file ->
            if (file.exists() && file.readText().isNotEmpty()) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "App Logs - Random Image")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "分享日志"))
            }
        }
    }

    fun clearLogs() {
        logBuffer.clear()
        logFile?.let { file ->
            file.delete()
            file.createNewFile()
        }
        Timber.d("Logs cleared")
    }
}
