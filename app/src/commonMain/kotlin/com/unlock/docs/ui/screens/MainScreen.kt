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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import com.unlock.docs.core.UnlockEngine
import com.unlock.docs.core.AuditLogger
import com.unlock.docs.core.NotificationManager
import com.unlock.docs.core.RuleEngine
import com.unlock.docs.core.SessionManager
import com.unlock.docs.core.SettingsManager
import com.unlock.docs.core.format.HandlerRegistry
import com.unlock.docs.i18n.LocalStrings
import com.unlock.docs.ui.components.FilePicker
import com.unlock.docs.utils.PatternGenerator
import com.unlock.docs.utils.WordlistReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.drop

enum class AttackMode { PATTERN, WORDLIST }

@OptIn(ExperimentalMaterial3Api::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
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
    var showResumeDialog by remember { mutableStateOf(false) }
    var pendingResumeOffset by remember { mutableStateOf(0L) }

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

    fun startBruteForce(path: String, offset: Long) {
        isRunning = true
        result = "${strings.progress}..."
        foundPassword = null
        currentChecked = offset
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
            val startTime = kotlinx.datetime.Clock.System.now().epochSeconds
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
                var lastSaveTime = 0L
                while (isActive) {
                    delay(1000)
                    val current = engine.progress.value + offset
                    currentChecked = current
                    speed = current - offset - lastProgress
                    lastProgress = current - offset

                    if (SettingsManager.isSessionResumptionEnabled()) {
                        val now = kotlinx.datetime.Clock.System.now().epochSeconds
                        if (now - lastSaveTime >= 10) { // Save every 10 seconds
                            SessionManager.saveSession(path, currentMode.name, current)
                            lastSaveTime = now
                        }
                    }
                }
            }

            val basePasswords =
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

            val droppedPasswords = if (offset > 0) basePasswords.drop(offset.toInt()) else basePasswords

            val passwords = if (SettingsManager.isAdvancedRulesEnabled()) {
                droppedPasswords.flatMapConcat { word -> RuleEngine.applyRules(word).asFlow() }
            } else {
                droppedPasswords
            }

            val found = engine.unlock(passwords, concurrency = threadCount.toInt())
            progressJob.cancel()

            val endTime = kotlinx.datetime.Clock.System.now().epochSeconds
            val duration = endTime - startTime

            foundPassword = found
            result =
                if (found != null) {
                    "${strings.passwordFound} $found"
                } else {
                    strings.passwordNotFound
                }

            if (SettingsManager.isAuditLoggingEnabled()) {
                AuditLogger.logAttack(
                    filePath = path,
                    mode = currentMode.name,
                    durationSeconds = duration,
                    result = if (found != null) "FOUND" else "NOT FOUND"
                )
            }

            if (SettingsManager.isNotificationsEnabled()) {
                NotificationManager.notifyCompletion(success = found != null)
            }
            
            if (found != null || offset + engine.progress.value >= totalProgress) {
                SessionManager.clearSession(path)
            }

            isRunning = false
            speed = 0L
            showResultDialog = true
        }
    }

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
                ProgressCard(
                    currentChecked = currentChecked,
                    totalProgress = totalProgress,
                    speed = speed,
                    strings = strings,
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    val path = selectedPath
                    if (path != null && !isRunning) {
                        val savedOffset = SessionManager.getSession(path)
                        if (SettingsManager.isSessionResumptionEnabled() && savedOffset != null && savedOffset > 0) {
                            pendingResumeOffset = savedOffset
                            showResumeDialog = true
                        } else {
                            // Start fresh
                            startBruteForce(path, 0L)
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

    if (showResumeDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showResumeDialog = false },
            title = { Text("Resume Session?") },
            text = { Text("A previous session was found for this file. Would you like to resume from $pendingResumeOffset?") },
            confirmButton = {
                Button(onClick = {
                    showResumeDialog = false
                    selectedPath?.let { startBruteForce(it, pendingResumeOffset) }
                }) {
                    Text("Resume")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showResumeDialog = false
                    selectedPath?.let { 
                        SessionManager.clearSession(it)
                        startBruteForce(it, 0L) 
                    }
                }) {
                    Text("Restart")
                }
            }
        )
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

@Composable
private fun ProgressCard(
    currentChecked: Long,
    totalProgress: Long,
    speed: Long,
    strings: com.unlock.docs.i18n.AppStrings,
    modifier: Modifier = Modifier
) {
    val progressValue = if (totalProgress > 0) {
        (currentChecked.toFloat() / totalProgress.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    
    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = progressValue,
        animationSpec = androidx.compose.material3.ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "ProgressAnimation"
    )

    val etaSeconds = if (speed > 0 && totalProgress > 0) {
        val remaining = totalProgress - currentChecked
        (remaining / speed).coerceAtLeast(0L)
    } else {
        -1L
    }

    val formattedEta = remember(etaSeconds) {
        if (etaSeconds < 0) {
            "--"
        } else if (etaSeconds < 60) {
            "${etaSeconds}s"
        } else {
            val minutes = etaSeconds / 60
            val remainingSeconds = etaSeconds % 60
            if (minutes < 60) {
                "${minutes}m ${remainingSeconds}s"
            } else {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                "${hours}h ${remainingMinutes}m"
            }
        }
    }

    ElevatedCard(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = strings.progress + "...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (totalProgress > 0) {
                    Text(
                        text = "${(progressValue * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (totalProgress > 0) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(5.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(5.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (speed > 0) "$speed/s" else "--",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Speed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Count",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (totalProgress > 0) "$currentChecked/$totalProgress" else "$currentChecked",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Checked",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "ETA",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedEta,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "ETA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
