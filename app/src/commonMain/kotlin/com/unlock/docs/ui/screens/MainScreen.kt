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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay

enum class AttackMode { PATTERN, WORDLIST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onNavigateToSettings: () -> Unit, onNavigateToAbout: () -> Unit) {
    val strings = LocalStrings.current
    val clipboardManager = LocalClipboardManager.current

    // Target File State
    var selectedPath by remember { mutableStateOf<String?>(null) }
    var showTargetPicker by remember { mutableStateOf(false) }

    // Engine State
    var result by remember { mutableStateOf<String?>(null) }
    var foundPassword by remember { mutableStateOf<String?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    var speed by remember { mutableStateOf(0L) }
    var currentChecked by remember { mutableStateOf(0L) }
    var totalProgress by remember { mutableStateOf(0L) }
    var showResultDialog by remember { mutableStateOf(false) }

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
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(Icons.Default.Info, contentDescription = strings.about)
                    }
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
            OutlinedTextField(
                value = selectedPath ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(strings.selectFile) },
                placeholder = { Text(strings.fileNotSelected) },
                trailingIcon = {
                    IconButton(onClick = { showTargetPicker = true }) {
                        Icon(Icons.Default.Folder, contentDescription = "Select File", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().animateContentSize(),
            )

            // Attack Mode Selection
            TabRow(
                selectedTabIndex = currentMode.ordinal,
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
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
            ElevatedCard(modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().animateContentSize()) {
                AnimatedContent(
                    targetState = currentMode,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut()
                            )
                        }.using(SizeTransform(clip = false))
                    },
                    label = "Attack Mode Transition"
                ) { mode ->
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
                                    label = { Text(strings.minLength) },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                )
                                OutlinedTextField(
                                    value = maxLen,
                                    onValueChange = { maxLen = it },
                                    label = { Text(strings.maxLength) },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                )
                            }
                        } else {
                            // Wordlist Configuration
                            var wordlistDropdownExpanded by remember { mutableStateOf(false) }
                            @OptIn(ExperimentalMaterial3Api::class)
                            ExposedDropdownMenuBox(
                                expanded = wordlistDropdownExpanded,
                                onExpandedChange = { wordlistDropdownExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = wordlistPath ?: strings.fileNotSelected,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(strings.wordlistMode) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wordlistDropdownExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                                )
                                ExposedDropdownMenu(
                                    expanded = wordlistDropdownExpanded,
                                    onDismissRequest = { wordlistDropdownExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(strings.localFile) },
                                        onClick = { 
                                            wordlistDropdownExpanded = false
                                            showWordlistPicker = true 
                                        },
                                        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(strings.download) },
                                        onClick = { 
                                            wordlistDropdownExpanded = false
                                            showWordlistManager = true 
                                        },
                                        leadingIcon = { Icon(Icons.Default.CloudDownload, contentDescription = null) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Thread Selection
            Column(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("${strings.threads}: ${threadCount.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
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

            AnimatedVisibility(
                visible = isRunning,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    if (totalProgress > 0) {
                        val progressValue = (currentChecked.toFloat() / totalProgress.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progressValue },
                            modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(0.8f).height(6.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(progressValue * 100).toInt()}% ($currentChecked / $totalProgress)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LinearProgressIndicator(modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(0.8f).height(6.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$currentChecked",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (speed > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${strings.speed}: $speed ${strings.passwordsPerSec}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val path = selectedPath
                    if (path != null && !isRunning) {
                        isRunning = true
                        result = "${strings.progress}..."
                        foundPassword = null
                        currentChecked = 0L
                        totalProgress = if (currentMode == AttackMode.PATTERN) {
                            PatternGenerator.countPasswords(
                                pattern,
                                minLen.toIntOrNull() ?: 1,
                                maxLen.toIntOrNull() ?: 4
                            )
                        } else {
                            wordlistPath?.let { WordlistReader.countPasswords(it) } ?: 0L
                        }

                        coroutineScope.launch(Dispatchers.Default) {
                            val lower = path.lowercase()
                            val handler = if (lower.endsWith(".zip")) HandlerRegistry.zipHandler else HandlerRegistry.officeHandler

                            if (handler == null) {
                                result = "Unsupported format"
                                isRunning = false
                                return@launch
                            }

                            val engine = UnlockEngine(handler, path)
                            
                            val progressJob = launch {
                                var lastProgress = 0L
                                while (isActive) {
                                    delay(1000)
                                    val current = engine.progress.value
                                    currentChecked = current
                                    speed = current - lastProgress
                                    lastProgress = current
                                }
                            }

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
                            progressJob.cancel()
                            
                            foundPassword = found
                            result =
                                if (found != null) {
                                    "${strings.passwordFound} $found"
                                } else {
                                    strings.passwordNotFound
                                }
                            isRunning = false
                            speed = 0L
                            showResultDialog = true
                        }
                    }
                },
                enabled = isStartEnabled,
                modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(0.8f).height(50.dp),
            ) {
                Text(if (isRunning) strings.stopBruteForce else strings.startBruteForce)
            }
        }
    }

    if (showResultDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text(strings.appName) },
            text = { 
                Text(
                    text = result ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (result?.startsWith(strings.passwordFound) == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            },
            confirmButton = {
                Button(onClick = { showResultDialog = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                if (foundPassword != null) {
                    Button(onClick = {
                        clipboardManager.setText(AnnotatedString(foundPassword!!))
                        showResultDialog = false
                    }) {
                        Text(strings.copy)
                    }
                }
            }
        )
    }

    FilePicker(
        show = showTargetPicker,
        allowedExtensions = listOf("zip", "rar", "7z", "doc", "docx", "xls", "xlsx", "ppt", "pptx")
    ) { path ->
        showTargetPicker = false
        if (path != null) selectedPath = path
    }

    FilePicker(
        show = showWordlistPicker,
        allowedExtensions = listOf("txt", "csv")
    ) { path ->
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
