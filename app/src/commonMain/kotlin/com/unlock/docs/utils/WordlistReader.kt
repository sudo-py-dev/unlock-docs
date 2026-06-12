package com.unlock.docs.utils

import kotlinx.coroutines.flow.Flow

expect object WordlistReader {
    fun readPasswords(filePath: String): Flow<String>
}
