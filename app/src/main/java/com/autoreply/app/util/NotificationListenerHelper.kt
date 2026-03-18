package com.autoreply.app.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.autoreply.app.service.WhatsAppReplyService

object NotificationListenerHelper {

    fun isEnabled(context: Context): Boolean {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        val names = flat.split(":")
        val component = ComponentName(context, WhatsAppReplyService::class.java)
        val ourFlat = component.flattenToString()
        return names.any { it.trim().equals(ourFlat, ignoreCase = true) }
    }

    fun openNotificationListenerSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
