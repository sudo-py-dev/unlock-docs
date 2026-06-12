package com.unlock.docs.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unlock.docs.core.UnlockEngine
import com.unlock.docs.core.format.HandlerRegistry
import com.unlock.docs.i18n.LocalStrings
import com.unlock.docs.ui.components.FilePicker
import com.unlock.docs.utils.PatternGenerator
import com.unlock.docs.utils.WordlistReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class AttackMode { PATTERN, WORDLIST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onNavigateToSettings: () -> Unit) {
    val strings = LocalStrings.current

    // Target File State
    var selectedPath by remember { mutableStateOf<String?>(null) }
    var showTargetPicker by remember { mutableStateOf(false) }

    // Engine State
    var result by remember { mutableStateOf<String?>(null) }
    var isRunning by remember { mutableStateOf(false) }

    // Mode State
    var currentMode by remember { mutableStateOf(AttackMode.PATTERN) }

    // Pattern State
    var pattern by remember { mutableStateOf("0123456789") }
    var minLen by remember { mutableStateOf("1") }
    var maxLen by remember { mutableStateOf("4") }

    // Wordlist State
    var wordlistPath by remember { mutableStateOf<String?>(null) }
    var showWordlistPicker by remember { mutableStateOf(false) }
    var showWordlistManager by remember { mutableStateOf(false) }

    // Thread State
    var threadCount by remember { mutableStateOf(1f) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.appName, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = strings.settings)
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Target File Selection Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Folder Icon",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { showTargetPicker = true },
                        modifier = Modifier.fillMaxWidth(0.8f),
                    ) {
                        Text(strings.selectFile)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = selectedPath ?: strings.fileNotSelected,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Attack Mode Selection
            TabRow(
                selectedTabIndex = currentMode.ordinal,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Tab(
                    selected = currentMode == AttackMode.PATTERN,
                    onClick = { currentMode = AttackMode.PATTERN },
                    text = { Text(strings.patternMode, fontWeight = FontWeight.SemiBold) },
                )
                Tab(
                    selected = currentMode == AttackMode.WORDLIST,
                    onClick = { currentMode = AttackMode.WORDLIST },
                    text = { Text(strings.wordlistMode, fontWeight = FontWeight.SemiBold) },
                )
            }

            // Attack Configuration Card
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Crossfade(targetState = currentMode) { mode ->
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (mode == AttackMode.PATTERN) {
                            OutlinedTextField(
                                value = pattern,
                                onValueChange = { pattern = it },
                                label = { Text(strings.patternMode) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = minLen,
                                    onValueChange = { minLen = it },
                                    label = { Text("Min Length") },
                                    modifier = Modifier.weight(1f),
                                )
                                OutlinedTextField(
                                    value = maxLen,
                                    onValueChange = { maxLen = it },
                                    label = { Text("Max Length") },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        } else {
                            // Wordlist Configuration
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Wordlist Icon",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth(0.8f),
                            ) {
                                Button(
                                    onClick = { showWordlistPicker = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                ) {
                                    Text("Local File")
                                }
                                Button(
                                    onClick = { showWordlistManager = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                ) {
                                    Text("Download")
                                }
                            }
                            Text(
                                text = wordlistPath ?: strings.fileNotSelected,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            // Thread Selection
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Threads: ${threadCount.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = threadCount,
                    onValueChange = { threadCount = it },
                    valueRange = 1f..8f,
                    steps = 6,
                    modifier = Modifier.fillMaxWidth(0.8f),
                )
            }

            // Start Button
            val isStartEnabled =
                selectedPath != null && !isRunning &&
                    (currentMode == AttackMode.PATTERN || wordlistPath != null)

            Button(
                onClick = {
                    val path = selectedPath
                    if (path != null && !isRunning) {
                        isRunning = true
                        result = "${strings.progress}..."
                        coroutineScope.launch(Dispatchers.Default) {
                            val lower = path.lowercase()
                            val handler = if (lower.endsWith(".zip")) HandlerRegistry.zipHandler else HandlerRegistry.officeHandler

                            if (handler == null) {
                                result = "Unsupported format"
                                isRunning = false
                                return@launch
                            }

                            val engine = UnlockEngine(handler, path)

                            val passwords =
                                if (currentMode == AttackMode.PATTERN) {
                                    PatternGenerator.generatePasswords(
                                        pattern,
                                        minLen.toIntOrNull() ?: 1,
                                        maxLen.toIntOrNull() ?: 4,
                                    )
                                } else {
                                    wordlistPath?.let { WordlistReader.readPasswords(it) }
                                        ?: kotlinx.coroutines.flow.emptyFlow()
                                }

                            val found = engine.unlock(passwords, concurrency = threadCount.toInt())
                            result =
                                if (found != null) {
                                    "${strings.passwordFound} $found"
                                } else {
                                    strings.passwordNotFound
                                }
                            isRunning = false
                        }
                    }
                },
                enabled = isStartEnabled,
                modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
            ) {
                Text(if (isRunning) strings.stopBruteForce else strings.startBruteForce)
            }

            AnimatedVisibility(visible = isRunning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.8f))
            }

            AnimatedVisibility(visible = result != null && !isRunning) {
                ElevatedCard(
                    colors =
                        CardDefaults.elevatedCardColors(
                            containerColor =
                                if (result?.startsWith(strings.passwordFound) == true) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.errorContainer
                                },
                        ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = result ?: "",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color =
                            if (result?.startsWith(strings.passwordFound) == true) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            },
                    )
                }
            }
        }
    }

    FilePicker(show = showTargetPicker) { path ->
        showTargetPicker = false
        if (path != null) selectedPath = path
    }

    FilePicker(show = showWordlistPicker) { path ->
        showWordlistPicker = false
        if (path != null) wordlistPath = path
    }

    if (showWordlistManager) {
        com.unlock.docs.ui.components.WordlistManagerDialog(
            onDismiss = { showWordlistManager = false },
            onWordlistSelected = { path ->
                wordlistPath = path
                showWordlistManager = false
            },
        )
    }
}
