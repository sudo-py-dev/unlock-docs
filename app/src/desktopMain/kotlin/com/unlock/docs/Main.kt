package com.unlock.docs

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.unlock.docs.core.format.HandlerRegistry
import com.unlock.docs.core.format.OfficeHandlerImpl
import com.unlock.docs.core.format.ZipHandlerImpl
import com.unlock.docs.ui.screens.App

fun main() =
    application {
        HandlerRegistry.zipHandler = ZipHandlerImpl()
        HandlerRegistry.officeHandler = OfficeHandlerImpl()

        Window(onCloseRequest = ::exitApplication, title = "Unlock Docs") {
            App()
        }
    }
