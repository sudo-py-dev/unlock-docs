package com.unlock.docs.core

actual object NotificationManager {
    actual fun notifyCompletion(success: Boolean) {
        try {
            val toolkitClass = Class.forName("java.awt.Toolkit")
            val getDefaultToolkitMethod = toolkitClass.getMethod("getDefaultToolkit")
            val toolkitInstance = getDefaultToolkitMethod.invoke(null)
            val beepMethod = toolkitClass.getMethod("beep")
            beepMethod.invoke(toolkitInstance)
        } catch (e: Throwable) {
            // Fallback or ignore on Android/headless JVMs
        }
    }
}
