package com.unlock.docs.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.unlock.docs.core.SettingsManager
import com.unlock.docs.i18n.ProvideAppStrings
import com.unlock.docs.ui.theme.AppTheme

@Composable
fun App() {
    var themeMode by remember { mutableStateOf(SettingsManager.getTheme()) }
    var language by remember { mutableStateOf(SettingsManager.getLanguage()) }
    var currentScreen by remember { mutableStateOf("main") }

    ProvideAppStrings(language = language) {
        AppTheme(themeMode = themeMode) {
            Box(modifier = Modifier.fillMaxSize()) {
                MainScreen(
                    onNavigateToSettings = { currentScreen = "settings" },
                    onNavigateToAbout = { currentScreen = "about" }
                )

                AnimatedVisibility(
                    visible = currentScreen == "settings",
                    enter = fadeIn() + scaleIn(initialScale = 0.92f),
                    exit = fadeOut() + scaleOut(targetScale = 0.92f),
                ) {
                    SettingsScreen(
                        currentTheme = themeMode,
                        onThemeChange = {
                            themeMode = it
                            SettingsManager.saveTheme(it)
                        },
                        currentLanguage = language,
                        onLanguageChange = {
                            language = it
                            SettingsManager.saveLanguage(it)
                        },
                        onBack = { currentScreen = "main" },
                    )
                }

                AnimatedVisibility(
                    visible = currentScreen == "about",
                    enter = fadeIn() + scaleIn(initialScale = 0.92f),
                    exit = fadeOut() + scaleOut(targetScale = 0.92f),
                ) {
                    AboutScreen(onBack = { currentScreen = "main" })
                }
            }
        }
    }
}
