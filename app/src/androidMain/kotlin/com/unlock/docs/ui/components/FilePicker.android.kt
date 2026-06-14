package com.unlock.docs.ui.components

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import java.io.File

private fun getFileName(context: android.content.Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        } catch (e: Exception) {
            // Log or ignore
        } finally {
            cursor?.close()
        }
    }
    if (result.isNullOrBlank()) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result?.ifBlank { null } ?: "temp_target_file"
}

private fun getMimeType(extensions: List<String>): String {
    if (extensions.size == 1) {
        return when (extensions[0].lowercase()) {
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "7z" -> "application/x-7z-compressed"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "txt" -> "text/plain"
            "csv" -> "text/csv"
            else -> "*/*"
        }
    }
    return "*/*"
}

@Composable
actual fun FilePicker(
    show: Boolean,
    allowedExtensions: List<String>,
    onFileSelected: (String?) -> Unit,
) {
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                try {
                    val fileName = getFileName(context, uri)
                    val file = File(context.cacheDir, fileName)
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    onFileSelected(file.absolutePath)
                } catch (e: Exception) {
                    onFileSelected(null)
                }
            } else {
                onFileSelected(null)
            }
        }

    if (show) {
        LaunchedEffect(Unit) {
            launcher.launch(getMimeType(allowedExtensions))
        }
    }
}
