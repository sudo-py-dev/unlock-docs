package com.unlock.docs.i18n

import androidx.compose.runtime.staticCompositionLocalOf

data class AppStrings(
    val appName: String,
    val unlock: String,
    val selectFile: String,
    val fileNotSelected: String,
    val wordlistMode: String,
    val patternMode: String,
    val startBruteForce: String,
    val stopBruteForce: String,
    val progress: String,
    val passwordFound: String,
    val passwordNotFound: String,
    val settings: String,
    val themeMode: String,
    val language: String,
    val light: String,
    val dark: String,
    val system: String,
)

val EnglishStrings =
    AppStrings(
        appName = "Unlock Docs",
        unlock = "Unlock",
        selectFile = "Select File",
        fileNotSelected = "No file selected",
        wordlistMode = "Wordlist",
        patternMode = "Pattern",
        startBruteForce = "Start",
        stopBruteForce = "Stop",
        progress = "Progress",
        passwordFound = "Password Found:",
        passwordNotFound = "Password Not Found",
        settings = "Settings",
        themeMode = "Theme",
        language = "Language",
        light = "Light",
        dark = "Dark",
        system = "System",
    )

val RussianStrings =
    AppStrings(
        appName = "Unlock Docs",
        unlock = "Разблокировать",
        selectFile = "Выбрать файл",
        fileNotSelected = "Файл не выбран",
        wordlistMode = "Словарь",
        patternMode = "Шаблон",
        startBruteForce = "Начать",
        stopBruteForce = "Остановить",
        progress = "Прогресс",
        passwordFound = "Пароль найден:",
        passwordNotFound = "Пароль не найден",
        settings = "Настройки",
        themeMode = "Тема",
        language = "Язык",
        light = "Светлая",
        dark = "Темная",
        system = "Системная",
    )

val HebrewStrings =
    AppStrings(
        appName = "Unlock Docs",
        unlock = "פתיחה",
        selectFile = "בחר קובץ",
        fileNotSelected = "לא נבחר קובץ",
        wordlistMode = "מילון",
        patternMode = "תבנית",
        startBruteForce = "התחל",
        stopBruteForce = "עצור",
        progress = "התקדמות",
        passwordFound = "סיסמה נמצאה:",
        passwordNotFound = "סיסמה לא נמצאה",
        settings = "הגדרות",
        themeMode = "ערכת נושא",
        language = "שפה",
        light = "בהיר",
        dark = "כהה",
        system = "מערכת",
    )

val LocalStrings = staticCompositionLocalOf { EnglishStrings }
