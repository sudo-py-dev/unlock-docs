package com.unlock.docs.core.format

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.FileHeader

class ZipHandlerImpl : ArchiveHandler {
    private var filePath: String? = null
    private var firstHeader: FileHeader? = null

    // ThreadLocal avoids concurrent modification of ZipFile's password state
    // while reusing instances for maximum performance.
    private val zipFileThreadLocal =
        object : ThreadLocal<ZipFile>() {
            override fun initialValue(): ZipFile? {
                val path = filePath ?: return null
                return ZipFile(path)
            }
        }

    override fun initialize(filePath: String): Boolean {
        this.filePath = filePath
        return try {
            val zipFile = ZipFile(filePath)
            if (!zipFile.isValidZipFile || !zipFile.isEncrypted) return false

            val headers = zipFile.fileHeaders
            if (headers.isNotEmpty()) {
                firstHeader = headers[0] as FileHeader
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun checkPassword(password: String): Boolean {
        val zipFile = zipFileThreadLocal.get() ?: return false
        val header = firstHeader ?: return false

        zipFile.setPassword(password.toCharArray())

        return try {
            val stream = zipFile.getInputStream(header)
            stream.read()
            stream.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
