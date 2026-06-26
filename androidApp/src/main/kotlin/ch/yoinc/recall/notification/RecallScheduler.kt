package ch.yoinc.recall.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import ch.yoinc.recall.Day
import ch.yoinc.recall.Recall
import java.util.Calendar

object RecallScheduler {

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    fun schedule(context: Context, recall: Recall) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (day in recall.days) {
            val triggerMillis = nextOccurrenceMillis(day, recall.time?.hour ?: 12, recall.time?.minute ?: 0)
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                alarmPendingIntent(context, recall, day),
            )
        }
    }

    fun cancel(context: Context, recall: Recall) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (day in Day.entries) {
            alarmManager.cancel(alarmPendingIntent(context, recall, day))
        }
    }

    internal fun alarmPendingIntent(context: Context, recall: Recall, day: Day): PendingIntent {
        val intent = Intent(context, RecallAlarmReceiver::class.java).apply {
            putExtra("recall_id", recall.id)
            putExtra("recall_text", recall.text)
            putExtra("recall_description", recall.description)
            putExtra("recall_day_ordinal", day.ordinal)
            putExtra("recall_hour", recall.time?.hour ?: 12)
            putExtra("recall_minute", recall.time?.minute ?: 0)
        }
        val requestCode = recall.id.hashCode() * 10 + day.ordinal
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun nextOccurrenceMillis(day: Day, hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayDow = now.get(Calendar.DAY_OF_WEEK)
        val targetDow = day.toCalendarDayOfWeek()
        var daysToAdd = (targetDow - todayDow + 7) % 7
        if (daysToAdd == 0 && !target.after(now)) daysToAdd = 7
        target.add(Calendar.DAY_OF_YEAR, daysToAdd)
        return target.timeInMillis
    }

    private fun Day.toCalendarDayOfWeek(): Int = when (this) {
        Day.MONDAY -> Calendar.MONDAY
        Day.TUESDAY -> Calendar.TUESDAY
        Day.WEDNESDAY -> Calendar.WEDNESDAY
        Day.THURSDAY -> Calendar.THURSDAY
        Day.FRIDAY -> Calendar.FRIDAY
        Day.SATURDAY -> Calendar.SATURDAY
        Day.SUNDAY -> Calendar.SUNDAY
    }
}
