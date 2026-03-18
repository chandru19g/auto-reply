package com.autoreply.app.ui

import android.content.Context
import android.content.pm.PackageManager
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autoreply.app.util.NotificationListenerHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scroll = rememberScrollState()
    val versionText = remember(context) { getAppVersion(context) }
    val notifEnabled = NotificationListenerHelper.isEnabled(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About & Help", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(scroll)
        ) {
            Text(
                text = "Auto-Reply sends automatic responses when a mode is ON (Sleeping, Working, Driving).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )

            SettingsSectionLabel("Quick status")
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                SettingsGroup {
                    AboutInfoRow(
                        icon = if (notifEnabled) Icons.Rounded.CheckCircle else Icons.Rounded.NotificationsOff,
                        title = "Notification access (WhatsApp)",
                        subtitle = if (notifEnabled) "Enabled" else "Disabled — enable it to allow WhatsApp replies"
                    )
                }
            }

            SettingsSectionLabel("Setup (recommended order)")
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                SettingsGroup {
                    StepRow(
                        step = "1",
                        icon = Icons.Rounded.EditNote,
                        title = "Edit your messages",
                        subtitle = "Open Settings → Edit messages and customise replies for each mode."
                    )
                    SettingsDivider()
                    StepRow(
                        step = "2",
                        icon = Icons.Rounded.People,
                        title = "Add whitelist contacts (optional)",
                        subtitle = "Whitelist contacts can always reach you even when a mode is ON."
                    )
                    SettingsDivider()
                    StepRow(
                        step = "3",
                        icon = Icons.Rounded.Sms,
                        title = "Grant SMS & Phone permissions",
                        subtitle = "Required to detect calls/SMS and send SMS replies."
                    )
                    SettingsDivider()
                    StepRow(
                        step = "4",
                        icon = Icons.Rounded.Chat,
                        title = "Enable notification access for WhatsApp",
                        subtitle = "Required to reply to WhatsApp messages via notification actions."
                    )
                }
            }

            SettingsSectionLabel("How it works")
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                SettingsGroup {
                    AboutInfoRow(
                        icon = Icons.Rounded.Call,
                        title = "Calls",
                        subtitle = "When a call comes in and a mode is ON, the app sends an SMS auto-reply to the caller. Call rejection depends on Android/device support."
                    )
                    SettingsDivider()
                    AboutInfoRow(
                        icon = Icons.Rounded.Sms,
                        title = "SMS",
                        subtitle = "Replies directly using SMS. Business/short-code senders are skipped."
                    )
                    SettingsDivider()
                    AboutInfoRow(
                        icon = Icons.Rounded.Chat,
                        title = "WhatsApp",
                        subtitle = "Replies using the WhatsApp notification reply button. Group chats and business accounts are skipped."
                    )
                }
            }

            SettingsSectionLabel("Testing checklist")
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                SettingsGroup {
                    BulletRow(
                        icon = Icons.Rounded.Bedtime,
                        title = "Turn on any mode",
                        body = "From the main screen choose Sleeping / Working / Driving."
                    )
                    SettingsDivider()
                    BulletRow(
                        icon = Icons.Rounded.PhoneAndroid,
                        title = "Test calls",
                        body = "Call your device from another phone. You should receive an SMS auto-reply on the caller device."
                    )
                    SettingsDivider()
                    BulletRow(
                        icon = Icons.Rounded.Sms,
                        title = "Test SMS",
                        body = "Send an SMS to your device. You should receive a single SMS reply."
                    )
                    SettingsDivider()
                    BulletRow(
                        icon = Icons.Rounded.Chat,
                        title = "Test WhatsApp",
                        body = "Send a 1:1 WhatsApp message. Make sure notification access is enabled. You should see one reply."
                    )
                    SettingsDivider()
                    BulletRow(
                        icon = Icons.Rounded.ReceiptLong,
                        title = "Check Session activity",
                        body = "Open Settings → Session activity to see who contacted you during this session."
                    )
                }
            }

            SettingsSectionLabel("Troubleshooting")
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                SettingsGroup {
                    AboutInfoRow(
                        icon = Icons.Rounded.Warning,
                        title = "WhatsApp reply not sending",
                        subtitle = "Enable notification access. Also ensure WhatsApp notifications are allowed and not silenced."
                    )
                    SettingsDivider()
                    AboutInfoRow(
                        icon = Icons.Rounded.Warning,
                        title = "SMS reply not sending",
                        subtitle = "Ensure SMS permissions are granted and your SIM can send SMS."
                    )
                    SettingsDivider()
                    AboutInfoRow(
                        icon = Icons.Rounded.Warning,
                        title = "Call not rejected",
                        subtitle = "Some devices/versions restrict call control. The SMS auto-reply can still work even if call rejection fails."
                    )
                    SettingsDivider()
                    AboutInfoRow(
                        icon = Icons.Rounded.TipsAndUpdates,
                        title = "Too many replies",
                        subtitle = "We include deduplication to avoid loops. If you still see repeats, share a Logcat snippet so we can tune the keying."
                    )
                }
            }

            SettingsSectionLabel("Privacy")
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                SettingsGroup {
                    AboutInfoRow(
                        icon = Icons.Rounded.PrivacyTip,
                        title = "Local-only storage",
                        subtitle = "Your name, message templates, whitelist, and session activity are stored only on your device."
                    )
                    SettingsDivider()
                    AboutInfoRow(
                        icon = Icons.Rounded.Lock,
                        title = "No server",
                        subtitle = "The app does not send your data to any backend."
                    )
                }
            }

            SettingsSectionLabel("App")
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                SettingsGroup {
                    AboutInfoRow(
                        icon = Icons.Rounded.Lock,
                        title = "Version",
                        subtitle = versionText
                    )
                    SettingsDivider()
                    AboutInfoRow(
                        icon = Icons.Rounded.BugReport,
                        title = "Need help?",
                        subtitle = "If something fails, capture Logcat (FATAL EXCEPTION) and share your Android version and device model."
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AboutInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StepRow(step: String, icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = step,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BulletRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun getAppVersion(context: Context): String = try {
    val pm = context.packageManager
    val pkg = context.packageName
    val pInfo = if (android.os.Build.VERSION.SDK_INT >= 33) {
        pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        pm.getPackageInfo(pkg, 0)
    }
    val name = pInfo.versionName ?: "?"
    val code = if (android.os.Build.VERSION.SDK_INT >= 28) pInfo.longVersionCode else {
        @Suppress("DEPRECATION")
        pInfo.versionCode.toLong()
    }
    "$name ($code)"
} catch (_: Exception) {
    "Unknown"
}

