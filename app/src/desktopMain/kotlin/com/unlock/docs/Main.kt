package com.unlock.docs

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.unlock.docs.core.format.HandlerRegistry
import com.unlock.docs.core.format.OfficeHandlerImpl
import com.unlock.docs.core.format.ZipHandlerImpl
import com.unlock.docs.ui.screens.App

import com.unlock.docs.core.SettingsManager

fun main() =
    application {
        HandlerRegistry.zipHandler = ZipHandlerImpl()
        HandlerRegistry.officeHandler = OfficeHandlerImpl()

        val strings = SettingsManager.getLanguage().strings

        Window(
            onCloseRequest = ::exitApplication,
            title = strings.appName,
            state = rememberWindowState(
                width = 800.dp,
                height = 600.dp,
                position = WindowPosition(Alignment.Center)
            )
        ) {
            App()
        }
    }
