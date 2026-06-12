package com.unlock.docs.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun FilePicker(
    show: Boolean,
    allowedExtensions: List<String> = emptyList(),
    onFileSelected: (String?) -> Unit,
)
