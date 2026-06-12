package com.unlock.docs.core

import com.unlock.docs.i18n.AppLanguage
import com.unlock.docs.ui.theme.ThemeMode

expect object SettingsManager {
    fun getTheme(): ThemeMode

    fun saveTheme(theme: ThemeMode)

    fun getLanguage(): AppLanguage

    fun saveLanguage(language: AppLanguage)
}
