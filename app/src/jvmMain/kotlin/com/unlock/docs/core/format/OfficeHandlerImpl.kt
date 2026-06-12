package com.unlock.docs.core.format

import org.apache.poi.poifs.crypt.Decryptor
import org.apache.poi.poifs.crypt.EncryptionInfo
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import java.io.File

class OfficeHandlerImpl : ArchiveHandler {
    private var filePath: String? = null

    // We cache POIFSFileSystem and Decryptor per-thread.
    // POIFSFileSystem parsing can be expensive, and Decryptor maintains state.
    private val threadLocalDecryptor =
        object : ThreadLocal<Pair<POIFSFileSystem, Decryptor>?>() {
            override fun initialValue(): Pair<POIFSFileSystem, Decryptor>? {
                val path = filePath ?: return null
                return try {
                    val fs = POIFSFileSystem(File(path), true) // true = read-only
                    val info = EncryptionInfo(fs)
                    val decryptor = Decryptor.getInstance(info)
                    Pair(fs, decryptor)
                } catch (e: Exception) {
                    null
                }
            }
        }

    override fun initialize(filePath: String): Boolean {
        this.filePath = filePath
        return try {
            val fs = POIFSFileSystem(File(filePath), true)
            val info = EncryptionInfo(fs)
            Decryptor.getInstance(info)
            fs.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun checkPassword(password: String): Boolean {
        val pair = threadLocalDecryptor.get() ?: return false
        val decryptor = pair.second

        return try {
            decryptor.verifyPassword(password)
        } catch (e: Exception) {
            false
        }
    }
}
