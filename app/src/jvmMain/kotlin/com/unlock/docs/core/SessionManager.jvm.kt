package com.unlock.docs.core

import java.io.File
import java.util.Properties
import java.io.FileInputStream
import java.io.FileOutputStream

actual object SessionManager {
    private val sessionFile by lazy {
        val dir = File(System.getProperty("user.home"), ".unlockdocs/sessions")
        if (!dir.exists()) dir.mkdirs()
        File(dir, "resumption.properties")
    }

    private fun loadProps(): Properties {
        val props = Properties()
        if (sessionFile.exists()) {
            try {
                FileInputStream(sessionFile).use { props.load(it) }
            } catch (e: Exception) {}
        }
        return props
    }

    private fun saveProps(props: Properties) {
        try {
            FileOutputStream(sessionFile).use { props.store(it, "UnlockDocs Sessions") }
        } catch (e: Exception) {}
    }

    actual fun saveSession(filePath: String, mode: String, offset: Long) {
        val props = loadProps()
        val key = filePath.hashCode().toString()
        props.setProperty(key, offset.toString())
        saveProps(props)
    }

    actual fun getSession(filePath: String): Long? {
        val props = loadProps()
        val key = filePath.hashCode().toString()
        return props.getProperty(key)?.toLongOrNull()
    }

    actual fun clearSession(filePath: String) {
        val props = loadProps()
        val key = filePath.hashCode().toString()
        props.remove(key)
        saveProps(props)
    }
}
