package com.unlock.docs.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.awt.FileDialog
import java.awt.Frame

@Composable
actual fun FilePicker(
    show: Boolean,
    allowedExtensions: List<String>,
    onFileSelected: (String?) -> Unit,
) {
    if (show) {
        LaunchedEffect(Unit) {
            val dialog = FileDialog(null as Frame?, "Select File", FileDialog.LOAD)
            if (allowedExtensions.isNotEmpty()) {
                dialog.file = allowedExtensions.joinToString(";") { "*.$it" }
                dialog.setFilenameFilter { _, name ->
                    allowedExtensions.any { ext -> name.endsWith(".$ext", ignoreCase = true) }
                }
            }
            dialog.isVisible = true
            if (dialog.directory != null && dialog.file != null) {
                onFileSelected(dialog.directory + dialog.file)
            } else {
                onFileSelected(null)
            }
        }
    }
}
