package com.unlock.docs.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import java.io.File

@Composable
actual fun FilePicker(
    show: Boolean,
    onFileSelected: (String?) -> Unit,
) {
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                try {
                    // Copy to cache to get a real File path for Zip4j/POI
                    val file = File(context.cacheDir, "temp_target_file")
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
            launcher.launch("*/*")
        }
    }
}
