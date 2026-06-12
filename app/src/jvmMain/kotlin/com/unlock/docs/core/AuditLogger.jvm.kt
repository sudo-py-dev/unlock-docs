package com.unlock.docs.core

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual object AuditLogger {
    private val logFile by lazy {
        val dir = File(System.getProperty("user.home"), ".unlockdocs")
        if (!dir.exists()) dir.mkdirs()
        File(dir, "audit.log")
    }

    actual fun logAttack(
        filePath: String,
        mode: String,
        durationSeconds: Long,
        result: String,
    ) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val entry = "[$timestamp] FILE: $filePath | MODE: $mode | DURATION: ${durationSeconds}s | RESULT: $result\n"
            logFile.appendText(entry)
        } catch (e: Exception) {
            // Silently fail logging
        }
    }
}
