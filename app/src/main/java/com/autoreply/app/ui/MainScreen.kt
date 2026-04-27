package com.autoreply.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Troubleshoot
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autoreply.app.domain.Mode
import com.autoreply.app.util.NotificationListenerHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    whitelistViewModel: WhitelistViewModel,
    onOpenSettings: () -> Unit,
    onOpenWhitelist: () -> Unit,
    sessionLogViewModel: SessionLogViewModel,
    onOpenSessionLog: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenAbout: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val appState by viewModel.appState.collectAsState()
    val showNameDialog by viewModel.showNameDialog.collectAsState()
    val whitelistContacts by whitelistViewModel.contacts.collectAsState()
    val sessionEvents by sessionLogViewModel.events.collectAsState()
    val context = LocalContext.current
    val notifEnabled = NotificationListenerHelper.isEnabled(context)
    var showTurnOffDialog by remember { mutableStateOf(false) }

    if (showTurnOffDialog) {
        AlertDialog(
            onDismissRequest = { showTurnOffDialog = false },
            title = { Text("Turn off and clear session?") },
            text = {
                Text(
                    "Turning off will remove the list of people who contacted you during this session (${sessionEvents.size} event${if (sessionEvents.size == 1) "" else "s"})."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    sessionLogViewModel.clear()
                    viewModel.setMode(Mode.OFF)
                    showTurnOffDialog = false
                }) { Text("Turn off") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showTurnOffDialog = false
                        onOpenSessionLog()
                    }) { Text("View list") }
                    TextButton(onClick = { showTurnOffDialog = false }) { Text("Cancel") }
                }
            }
        )
    }

    // Show name dialog on first launch until user provides their name
    if (showNameDialog) {
        NameInputDialog(onNameSaved = { viewModel.saveUserName(it) })
    }

    LaunchedEffect(appState.activeMode) {
        if (appState.activeMode != Mode.OFF) {
            val label = when (appState.activeMode) {
                Mode.SLEEPING -> "Sleeping"
                Mode.WORKING -> "Working"
                Mode.DRIVING -> "Driving"
                Mode.OFF -> ""
            }
            if (label.isNotEmpty()) snackbarHostState.showSnackbar("$label mode on — auto-reply active")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Auto-Reply",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Notification access warning (WhatsApp) — show at top
            if (!notifEnabled) {
                NotificationAccessWarningCard(
                    onClick = { NotificationListenerHelper.openNotificationListenerSettings(context) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Status banner
            ActiveModeBanner(activeMode = appState.activeMode)

            Spacer(modifier = Modifier.height(16.dp))

            // Mode selection group
            SettingsSectionLabel("Mode")
            SettingsGroup {
                ModeRow(
                    icon = Icons.Rounded.PowerSettingsNew,
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    title = "Off",
                    subtitle = "No auto-reply",
                    selected = appState.activeMode == Mode.OFF,
                    onClick = {
                        if (appState.activeMode != Mode.OFF && sessionEvents.isNotEmpty()) {
                            showTurnOffDialog = true
                        } else {
                            // No session activity → just turn off
                            sessionLogViewModel.clear()
                            viewModel.setMode(Mode.OFF)
                        }
                    }
                )
                SettingsDivider()
                ModeRow(
                    icon = Icons.Rounded.Bedtime,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    title = "Sleeping",
                    subtitle = "Auto-reply while you sleep",
                    selected = appState.activeMode == Mode.SLEEPING,
                    onClick = { viewModel.setMode(Mode.SLEEPING) }
                )
                SettingsDivider()
                ModeRow(
                    icon = Icons.Rounded.Work,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Working",
                    subtitle = "Auto-reply during work hours",
                    selected = appState.activeMode == Mode.WORKING,
                    onClick = { viewModel.setMode(Mode.WORKING) }
                )
                SettingsDivider()
                ModeRow(
                    icon = Icons.Rounded.DirectionsCar,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "Driving",
                    subtitle = "Auto-reply while on the road",
                    selected = appState.activeMode == Mode.DRIVING,
                    onClick = { viewModel.setMode(Mode.DRIVING) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Settings group
            SettingsSectionLabel("Settings")
            SettingsGroup {
                SettingsNavRow(
                    icon = Icons.Rounded.EditNote,
                    iconBg = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Edit messages",
                    subtitle = "Update replies for each mode",
                    onClick = onOpenSettings
                )
                SettingsDivider()
                SettingsNavRow(
                    icon = Icons.Rounded.People,
                    iconBg = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "Whitelist",
                    subtitle = "${whitelistContacts.size} contact${if (whitelistContacts.size == 1) "" else "s"} can always reach you",
                    onClick = onOpenWhitelist
                )
                SettingsDivider()
                SettingsNavRow(
                    icon = Icons.Rounded.ReceiptLong,
                    iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    title = "Session activity",
                    subtitle = if (appState.activeMode == Mode.OFF) "Turn on a mode to start tracking"
                    else "${sessionEvents.size} event${if (sessionEvents.size == 1) "" else "s"} in this session",
                    onClick = onOpenSessionLog
                )
                SettingsDivider()
                SettingsNavRow(
                    icon = Icons.Rounded.Troubleshoot,
                    iconBg = MaterialTheme.colorScheme.errorContainer,
                    iconTint = MaterialTheme.colorScheme.error,
                    title = "Diagnostics",
                    subtitle = "Check notification, battery and permission status",
                    onClick = onOpenDiagnostics
                )
                SettingsDivider()
                SettingsNavRow(
                    icon = Icons.Rounded.Info,
                    iconBg = MaterialTheme.colorScheme.surfaceVariant,
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    title = "About & Help",
                    subtitle = "Setup, testing and troubleshooting",
                    onClick = onOpenAbout
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Status banner ─────────────────────────────────────────────────────────────

@Composable
private fun ActiveModeBanner(activeMode: Mode) {
    if (activeMode == Mode.OFF) return

    val (label, icon, bgColor, contentColor) = when (activeMode) {
        Mode.SLEEPING -> BannerStyle(
            "Sleeping mode — auto-reply is active",
            Icons.Rounded.Bedtime,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        Mode.WORKING -> BannerStyle(
            "Working mode — auto-reply is active",
            Icons.Rounded.Work,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        Mode.DRIVING -> BannerStyle(
            "Driving mode — auto-reply is active",
            Icons.Rounded.DirectionsCar,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        Mode.OFF -> return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

private data class BannerStyle(
    val label: String,
    val icon: ImageVector,
    val bgColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color
)

// ── Settings-style shared components ─────────────────────────────────────────

@Composable
fun SettingsSectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
    )
}

@Composable
fun SettingsGroup(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column { content() }
    }
}

@Composable
fun SettingsDivider() {
    Divider(
        modifier = Modifier.padding(start = 64.dp, end = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

// ── Mode row ──────────────────────────────────────────────────────────────────

@Composable
private fun ModeRow(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val iconBg by animateColorAsState(
        targetValue = if (selected)
            iconTint.copy(alpha = 0.15f)
        else
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(200),
        label = "iconBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) iconTint else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

// ── Notification access row ───────────────────────────────────────────────────

@Composable
private fun NotificationAccessRow(enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (enabled) Icons.Rounded.CheckCircle else Icons.Rounded.NotificationsOff,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Notification access",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (enabled) "Enabled — WhatsApp auto-reply is ready"
                else "Tap to enable for WhatsApp auto-reply",
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }

        if (!enabled) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Notification warning card (top) ───────────────────────────────────────────

@Composable
private fun NotificationAccessWarningCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notification access required",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Enable it to allow WhatsApp auto-reply",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Open",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Icon(
                    imageVector = Icons.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ── Generic settings navigation row ───────────────────────────────────────────

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    iconBg: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
