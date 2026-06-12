package com.unlock.docs.core

import com.unlock.docs.core.format.ArchiveHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UnlockEngine(
    private val handler: ArchiveHandler,
    private val filePath: String,
) {
    private val _progress = MutableStateFlow(0L)
    val progress = _progress.asStateFlow()
    suspend fun unlock(
        passwords: Flow<String>,
        concurrency: Int = 1,
    ): String? =
        coroutineScope {
            _progress.value = 0L
            if (!handler.initialize(filePath)) {
                return@coroutineScope null // Not supported or not encrypted
            }

            val channel = Channel<String>(capacity = 1000)
            val resultDeferred = kotlinx.coroutines.CompletableDeferred<String?>()

            // Producer
            val producer =
                launch(Dispatchers.Default) {
                    passwords.collect { pwd ->
                        if (resultDeferred.isCompleted) return@collect
                        channel.send(pwd)
                    }
                    channel.close()
                }

            // Consumers
            val jobs =
                List(concurrency) {
                    launch(Dispatchers.IO) {
                        var localCount = 0L
                        for (pwd in channel) {
                            if (resultDeferred.isCompleted) break
                            val success =
                                try {
                                    handler.checkPassword(pwd)
                                } catch (e: Exception) {
                                    false
                                }
                            
                            localCount++
                            if (localCount >= 100) {
                                _progress.update { it + localCount }
                                localCount = 0L
                            }

                            if (success) {
                                _progress.update { it + localCount }
                                resultDeferred.complete(pwd)
                                break
                            }
                        }
                        if (localCount > 0) {
                            _progress.update { it + localCount }
                        }
                    }
                }

            // Waiter to resolve null if all jobs finish without finding password
            launch(Dispatchers.Default) {
                jobs.forEach { it.join() }
                if (!resultDeferred.isCompleted) {
                    resultDeferred.complete(null)
                }
            }

            val foundPassword = resultDeferred.await()

            // Cleanup immediately after completion
            jobs.forEach { it.cancel() }
            producer.cancel()
            channel.cancel()

            foundPassword
        }
}
