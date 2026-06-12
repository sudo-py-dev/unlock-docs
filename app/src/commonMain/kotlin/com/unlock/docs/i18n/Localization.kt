package com.unlock.docs.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

expect fun getSystemLanguage(): String

enum class AppLanguage(val code: String, val displayName: String, val strings: AppStrings) {
    SYSTEM("system", "System", EnglishStrings),
    EN("en", "English", EnglishStrings),
    RU("ru", "Русский", RussianStrings),
    IW("iw", "עברית", HebrewStrings),
}

@Composable
fun ProvideAppStrings(
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    val strings =
        if (language == AppLanguage.SYSTEM) {
            when (getSystemLanguage()) {
                "ru" -> RussianStrings
                "iw", "he" -> HebrewStrings
                else -> EnglishStrings
            }
        } else {
            language.strings
        }

    val layoutDirection = if (strings === HebrewStrings) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalStrings provides strings,
        LocalLayoutDirection provides layoutDirection
    ) {
        content()
    }
}
