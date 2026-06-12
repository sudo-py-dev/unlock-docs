package com.unlock.docs.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unlock.docs.core.CuratedWordlist
import com.unlock.docs.core.DefaultWordlists
import com.unlock.docs.core.WordlistDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordlistManagerDialog(
    onDismiss: () -> Unit,
    onWordlistSelected: (String) -> Unit,
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Icons.Default.CloudDownload,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Curated Wordlists",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(24.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(DefaultWordlists) { wordlist ->
                        WordlistCard(wordlist, onWordlistSelected)
                    }
                }

                Spacer(Modifier.height(24.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun WordlistCard(
    wordlist: CuratedWordlist,
    onWordlistSelected: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var isDownloaded by remember { mutableStateOf(WordlistDownloader.isDownloaded(wordlist)) }
    var downloadProgress by remember { mutableStateOf<Float?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            if (isDownloaded) {
                WordlistDownloader.getLocalPath(wordlist)?.let { onWordlistSelected(it) }
            }
        },
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(wordlist.name, style = MaterialTheme.typography.titleMedium)
                    Text(wordlist.description, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${wordlist.expectedSizeMb} MB",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }

                if (isDownloaded) {
                    Icon(Icons.Default.Done, contentDescription = "Downloaded", tint = MaterialTheme.colorScheme.primary)
                } else if (downloadProgress == null) {
                    IconButton(onClick = {
                        downloadProgress = 0f
                        error = null
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                WordlistDownloader.downloadWordlist(wordlist).collect { progress ->
                                    downloadProgress = progress
                                }
                                isDownloaded = true
                                downloadProgress = null
                            } catch (e: Exception) {
                                error = "Download failed: ${e.message}"
                                downloadProgress = null
                            }
                        }
                    }) {
                        Icon(Icons.Default.CloudDownload, contentDescription = "Download")
                    }
                }
            }

            if (downloadProgress != null) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { downloadProgress ?: 0f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
