package ch.yoinc.recall.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import ch.yoinc.recall.Day
import ch.yoinc.recall.Recall
import ch.yoinc.recall.RecallActivity
import ch.yoinc.recall.RecallTime

class RecallAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val recallId = intent.getStringExtra("recall_id") ?: return
        val recallText = intent.getStringExtra("recall_text") ?: return
        val recallDescription = intent.getStringExtra("recall_description") ?: ""
        val dayOrdinal = intent.getIntExtra("recall_day_ordinal", 0)
        val hour = intent.getIntExtra("recall_hour", 12)
        val minute = intent.getIntExtra("recall_minute", 0)

        showNotification(context, recallId, recallText, recallDescription)

        // Reschedule for the same weekday next week
        val day = Day.entries[dayOrdinal]
        val recall = Recall(
            id = recallId,
            text = recallText,
            days = setOf(day),
            time = RecallTime(hour, minute),
            description = recallDescription,
        )
        try {
            RecallScheduler.schedule(context, recall)
        } catch (_: SecurityException) {
            // SCHEDULE_EXACT_ALARM permission was revoked; user must re-open the app
        }
    }

    private fun showNotification(context: Context, recallId: String, title: String, body: String) {
        val openIntent = Intent(context, RecallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("recall_id", recallId)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            recallId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .apply { if (body.isNotBlank()) setContentText(body) }
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setContentIntent(openPendingIntent)
            .setFullScreenIntent(openPendingIntent, true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(recallId.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "recall_alerts"
    }
}
