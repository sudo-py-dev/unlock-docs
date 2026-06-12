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
}
