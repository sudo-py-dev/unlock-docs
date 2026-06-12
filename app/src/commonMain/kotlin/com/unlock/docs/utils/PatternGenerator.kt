package com.unlock.docs.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object PatternGenerator {
    /**
     * Generates all combinations of characters from [charset] with lengths from [minLength] to [maxLength].
     */
    fun generatePasswords(
        charset: String,
        minLength: Int,
        maxLength: Int,
    ): Flow<String> =
        flow {
            if (charset.isEmpty() || minLength > maxLength || minLength < 1) return@flow

            for (len in minLength..maxLength) {
                val indices = IntArray(len)
                while (true) {
                    val sb = StringBuilder(len)
                    for (i in 0 until len) {
                        sb.append(charset[indices[i]])
                    }
                    emit(sb.toString())

                    var pos = len - 1
                    while (pos >= 0) {
                        indices[pos]++
                        if (indices[pos] < charset.length) {
                            break
                        }
                        indices[pos] = 0
                        pos--
                    }
                    if (pos < 0) break
                }
            }
        }

    fun countPasswords(
        charset: String,
        minLength: Int,
        maxLength: Int,
    ): Long {
        if (charset.isEmpty() || minLength > maxLength || minLength < 1) return 0
        var total = 0L
        val base = charset.length.toLong()
        for (len in minLength..maxLength) {
            var currentLenTotal = 1L
            var overflow = false
            for (i in 1..len) {
                if (currentLenTotal > Long.MAX_VALUE / base) {
                    overflow = true
                    break
                }
                currentLenTotal *= base
            }
            
            if (overflow || total > Long.MAX_VALUE - currentLenTotal) {
                return Long.MAX_VALUE
            }
            total += currentLenTotal
        }
        return total
    }
}
