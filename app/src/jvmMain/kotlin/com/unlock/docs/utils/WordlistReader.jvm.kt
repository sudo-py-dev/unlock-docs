package com.unlock.docs.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

actual object WordlistReader {
    actual fun readPasswords(filePath: String): Flow<String> =
        flow {
            try {
                File(filePath).useLines { lines ->
                    lines.forEach { emit(it) }
                }
            } catch (e: Exception) {
                // Ignore or handle errors gracefully
            }
        }
}
