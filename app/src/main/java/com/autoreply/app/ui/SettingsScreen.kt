package com.autoreply.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val appState by viewModel.appState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Messages",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
            Text(
                text = "Set the auto-reply text sent to callers and senders for each mode.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSectionLabel("Modes")

            SettingsGroup {
                ModeMessageSection(
                    icon = Icons.Rounded.Bedtime,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    modeLabel = "Sleeping",
                    callMessage = appState.sleepingCallMessage,
                    smsMessage = appState.sleepingSmsMessage,
                    whatsAppMessage = appState.sleepingWhatsAppMessage,
                    onSave = { call, sms, wa -> viewModel.setSleepingMessages(call, sms, wa) }
                )
                SettingsDivider()
                ModeMessageSection(
                    icon = Icons.Rounded.Work,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modeLabel = "Working",
                    callMessage = appState.workingCallMessage,
                    smsMessage = appState.workingSmsMessage,
                    whatsAppMessage = appState.workingWhatsAppMessage,
                    onSave = { call, sms, wa -> viewModel.setWorkingMessages(call, sms, wa) }
                )
                SettingsDivider()
                ModeMessageSection(
                    icon = Icons.Rounded.DirectionsCar,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    modeLabel = "Driving",
                    callMessage = appState.drivingCallMessage,
                    smsMessage = appState.drivingSmsMessage,
                    whatsAppMessage = appState.drivingWhatsAppMessage,
                    onSave = { call, sms, wa -> viewModel.setDrivingMessages(call, sms, wa) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ModeMessageSection(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    modeLabel: String,
    callMessage: String,
    smsMessage: String,
    whatsAppMessage: String,
    onSave: (call: String, sms: String, whatsApp: String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var call by rememberSaveable(callMessage) { mutableStateOf(callMessage) }
    var sms by rememberSaveable(smsMessage) { mutableStateOf(smsMessage) }
    var wa by rememberSaveable(whatsAppMessage) { mutableStateOf(whatsAppMessage) }

    val hasChanges = call != callMessage || sms != smsMessage || wa != whatsAppMessage

    // Header row — tapping expands/collapses
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
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
                text = modeLabel,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (expanded) "Editing messages" else "Tap to edit messages",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (hasChanges && expanded) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "Unsaved",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Icon(
            imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp
            else Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
    }

    // Expandable content
    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn(tween(150)) + expandVertically(tween(200)),
        exit = fadeOut(tween(100)) + shrinkVertically(tween(150))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MessageField(
                value = call,
                onValueChange = { call = it },
                label = "When someone calls",
                leadingIcon = Icons.Rounded.Call
            )
            MessageField(
                value = sms,
                onValueChange = { sms = it },
                label = "For SMS",
                leadingIcon = Icons.Rounded.Message
            )
            MessageField(
                value = wa,
                onValueChange = { wa = it },
                label = "For WhatsApp",
                leadingIcon = null,
                leadingEmoji = "🟢"
            )

            Button(
                onClick = {
                    onSave(call, sms, wa)
                    expanded = false
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = call.isNotBlank() || sms.isNotBlank() || wa.isNotBlank()
            ) {
                Text(
                    text = "Save $modeLabel messages",
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun MessageField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector?,
    leadingEmoji: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = when {
            leadingIcon != null -> ({
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            })
            leadingEmoji != null -> ({
                Text(
                    text = leadingEmoji,
                    style = MaterialTheme.typography.bodyLarge
                )
            })
            else -> null
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = false,
        maxLines = 3,
        shape = RoundedCornerShape(12.dp)
    )
}
