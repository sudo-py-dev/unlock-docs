package com.unlock.docs.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

actual object WordlistDownloader {
    private val cacheDir by lazy {
        val userHome = System.getProperty("user.home")
        val dir = File(userHome, ".unlockdocs/wordlists")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    private fun getFile(wordlist: CuratedWordlist): File {
        val safeName = wordlist.name.replace(Regex("[^a-zA-Z0-9.-]"), "_") + ".txt"
        return File(cacheDir, safeName)
    }

    actual fun isDownloaded(wordlist: CuratedWordlist): Boolean {
        return getFile(wordlist).exists()
    }

    actual fun getLocalPath(wordlist: CuratedWordlist): String? {
        val file = getFile(wordlist)
        return if (file.exists()) file.absolutePath else null
    }

    actual fun downloadWordlist(wordlist: CuratedWordlist): Flow<Float> =
        flow {
            withContext(Dispatchers.IO) {
                val file = getFile(wordlist)
                if (file.exists()) {
                    emit(1.0f)
                    return@withContext
                }

                var connection: HttpURLConnection? = null
                var outputStream: FileOutputStream? = null
                try {
                    val url = URL(wordlist.url)
                    connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    connection.connect()

                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        throw Exception("Server returned HTTP ${connection.responseCode} ${connection.responseMessage}")
                    }

                    val fileLength = connection.contentLength
                    val inputStream = connection.inputStream
                    outputStream = FileOutputStream(file)

                    val data = ByteArray(4096)
                    var total: Long = 0
                    var count: Int

                    emit(0.0f)

                    while (inputStream.read(data).also { count = it } != -1) {
                        total += count
                        outputStream.write(data, 0, count)
                        if (fileLength > 0) {
                            emit((total.toFloat() / fileLength.toFloat()))
                        }
                    }

                    emit(1.0f)
                } catch (e: Exception) {
                    if (file.exists()) file.delete()
                    throw e
                } finally {
                    outputStream?.close()
                    connection?.disconnect()
                }
            }
        }
}
