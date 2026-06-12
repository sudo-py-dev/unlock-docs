package com.unlock.docs.unlockdocs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.unlock.docs.core.format.HandlerRegistry
import com.unlock.docs.core.format.OfficeHandlerImpl
import com.unlock.docs.core.format.ZipHandlerImpl
import com.unlock.docs.ui.screens.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        HandlerRegistry.zipHandler = ZipHandlerImpl()
        HandlerRegistry.officeHandler = OfficeHandlerImpl()

        setContent {
            App()
        }
    }
}
