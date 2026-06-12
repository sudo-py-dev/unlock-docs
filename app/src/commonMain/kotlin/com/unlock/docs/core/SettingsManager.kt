package com.unlock.docs.core

import com.unlock.docs.i18n.AppLanguage
import com.unlock.docs.ui.theme.ThemeMode

expect object SettingsManager {
    fun getTheme(): ThemeMode

    fun saveTheme(theme: ThemeMode)

    fun getLanguage(): AppLanguage

    fun saveLanguage(language: AppLanguage)

    fun isAuditLoggingEnabled(): Boolean
    fun setAuditLoggingEnabled(enabled: Boolean)

    fun isNotificationsEnabled(): Boolean
    fun setNotificationsEnabled(enabled: Boolean)

    fun isAdvancedRulesEnabled(): Boolean
    fun setAdvancedRulesEnabled(enabled: Boolean)

    fun isSessionResumptionEnabled(): Boolean
    fun setSessionResumptionEnabled(enabled: Boolean)
}
