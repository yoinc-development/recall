package ch.yoinc.recall.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ch.yoinc.recall.data.RecallDatabase
import ch.yoinc.recall.data.RecallRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = RecallRepository(RecallDatabase.getInstance(context).recallDao())
                val recalls = repository.getAllRecalls().first()
                recalls.forEach { recall ->
                    try {
                        RecallScheduler.schedule(context, recall)
                    } catch (_: SecurityException) {
                        // exact alarm permission revoked; skip
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
