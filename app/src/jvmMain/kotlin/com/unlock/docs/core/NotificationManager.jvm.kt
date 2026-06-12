package com.unlock.docs.core

import java.awt.Toolkit

actual object NotificationManager {
    actual fun notifyCompletion(success: Boolean) {
        try {
            Toolkit.getDefaultToolkit().beep()
        } catch (e: Exception) {
            // Fallback or ignore
        }
    }
}
