package ch.yoinc.recall

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ch.yoinc.recall.data.RecallDatabase
import ch.yoinc.recall.data.RecallRepository
import ch.yoinc.recall.notification.RecallAlarmReceiver
import ch.yoinc.recall.notification.RecallScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecallViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RecallRepository(RecallDatabase.getInstance(application).recallDao())

    val recalls: StateFlow<List<Recall>> = repository.getAllRecalls()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addRecall(recall: Recall) {
        viewModelScope.launch {
            repository.insert(recall)
            if (RecallScheduler.canScheduleExactAlarms(getApplication())) {
                RecallScheduler.schedule(getApplication(), recall)
            } else {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                getApplication<Application>().startActivity(intent)
            }
        }
    }

    fun deleteRecall(id: String) {
        viewModelScope.launch {
            val recall = recalls.value.find { it.id == id }
            if (recall != null) RecallScheduler.cancel(getApplication(), recall)
            repository.delete(id)
        }
    }

    fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            RecallAlarmReceiver.CHANNEL_ID,
            "Recall Alerts",
            NotificationManager.IMPORTANCE_HIGH,
        )
        val manager = getApplication<Application>()
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
