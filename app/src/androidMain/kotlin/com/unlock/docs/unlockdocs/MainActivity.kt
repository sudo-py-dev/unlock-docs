package com.unlock.docs.unlockdocs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.unlock.docs.core.format.HandlerRegistry
import com.unlock.docs.core.format.OfficeHandlerImpl
import com.unlock.docs.core.format.ZipHandlerImpl
import com.unlock.docs.ui.screens.App

import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        System.setProperty("user.home", filesDir.absolutePath)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        HandlerRegistry.zipHandler = ZipHandlerImpl()
        HandlerRegistry.officeHandler = OfficeHandlerImpl()

        setContent {
            App()
        }
    }
}
