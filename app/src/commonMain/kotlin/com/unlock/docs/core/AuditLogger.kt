package com.unlock.docs.core

expect object AuditLogger {
    fun logAttack(
        filePath: String,
        mode: String,
        durationSeconds: Long,
        result: String,
    )
}
