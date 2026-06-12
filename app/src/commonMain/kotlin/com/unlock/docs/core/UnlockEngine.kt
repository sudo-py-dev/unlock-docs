package com.unlock.docs.core

import com.unlock.docs.core.format.ArchiveHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UnlockEngine(
    private val handler: ArchiveHandler,
    private val filePath: String,
) {
    suspend fun unlock(
        passwords: Flow<String>,
        concurrency: Int = 1,
    ): String? =
        coroutineScope {
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
                        for (pwd in channel) {
                            if (resultDeferred.isCompleted) break
                            val success =
                                try {
                                    handler.checkPassword(pwd)
                                } catch (e: Exception) {
                                    false
                                }
                            if (success) {
                                resultDeferred.complete(pwd)
                                break
                            }
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
