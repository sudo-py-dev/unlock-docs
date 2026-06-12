package com.unlock.docs.i18n

import java.util.Locale

actual fun getSystemLanguage(): String {
    return Locale.getDefault().language
}
