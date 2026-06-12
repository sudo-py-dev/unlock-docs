package com.unlock.docs.core

import com.unlock.docs.i18n.AppLanguage
import com.unlock.docs.ui.theme.ThemeMode
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

actual object SettingsManager {
    private val file by lazy {
        val dir = File(System.getProperty("user.home"), ".unlockdocs")
        if (!dir.exists()) dir.mkdirs()
        File(dir, "settings.properties")
    }

    private val props = Properties()

    init {
        if (file.exists()) {
            try {
                FileInputStream(file).use { props.load(it) }
            } catch (e: Exception) {
            }
        }
    }

    private fun save() {
        try {
            FileOutputStream(file).use { props.store(it, "UnlockDocs Settings") }
        } catch (e: Exception) {
        }
    }

    actual fun getTheme(): ThemeMode {
        val name = props.getProperty("theme", ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    actual fun saveTheme(theme: ThemeMode) {
        props.setProperty("theme", theme.name)
        save()
    }

    actual fun getLanguage(): AppLanguage {
        val name = props.getProperty("language", AppLanguage.SYSTEM.name)
        return try {
            AppLanguage.valueOf(name)
        } catch (e: Exception) {
            AppLanguage.SYSTEM
        }
    }

    actual fun saveLanguage(language: AppLanguage) {
        props.setProperty("language", language.name)
        save()
    }
}
