package com.unlock.docs.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

expect fun getSystemLanguage(): String

enum class AppLanguage(val code: String, val strings: AppStrings) {
    SYSTEM("system", EnglishStrings),
    EN("en", EnglishStrings),
    RU("ru", RussianStrings),
    IW("iw", HebrewStrings),
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

    CompositionLocalProvider(LocalStrings provides strings) {
        content()
    }
}
