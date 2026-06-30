package ch.yoinc.recall

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class RecallActivity : ComponentActivity() {
    private val viewModel: RecallViewModel by viewModels()
    private var notificationRecallId by mutableStateOf<String?>(null)
    private var permissionsGranted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        viewModel.setupNotificationChannel()
        permissionsGranted = missingPermissions().isEmpty()
        notificationRecallId = intent.getStringExtra("recall_id")
        cancelNotificationIfPresent(notificationRecallId)

        setContent {
            val darkTheme = isSystemInDarkTheme()
            val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)
            } else {
                if (darkTheme) darkColorScheme() else lightColorScheme()
            }
            MaterialTheme(colorScheme = colorScheme) {
                val recalls by viewModel.recalls.collectAsState()
                if (permissionsGranted) {
                    App(
                        recalls = recalls,
                        onAdd = viewModel::addRecall,
                        onDelete = viewModel::deleteRecall,
                        initialRecallId = notificationRecallId,
                    )
                } else {
                    PermissionScreen(permissions = missingPermissions())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        permissionsGranted = missingPermissions().isEmpty()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationRecallId = intent.getStringExtra("recall_id")
        cancelNotificationIfPresent(notificationRecallId)
    }

    private fun missingPermissions(): List<PermissionItem> {
        val missing = mutableListOf<PermissionItem>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                missing += PermissionItem(
                    label = "Alarms & reminders",
                    description = "Needed to deliver recalls at the exact time you scheduled.",
                    settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.fromParts("package", packageName, null)
                    },
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                missing += PermissionItem(
                    label = "Notifications",
                    description = "Needed to show recall reminders on your screen.",
                    settingsIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    },
                )
            }
        }

        return missing
    }

    private fun cancelNotificationIfPresent(recallId: String?) {
        if (recallId == null) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(recallId.hashCode())
    }
}

private data class PermissionItem(
    val label: String,
    val description: String,
    val settingsIntent: Intent,
)

@Composable
private fun PermissionScreen(permissions: List<PermissionItem>) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "Permissions required",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "recall needs the following permissions to work.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            permissions.forEach { permission ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = permission.label,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = permission.description,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = { context.startActivity(permission.settingsIntent) }) {
                        Text("Open Settings")
                    }
                }
            }
        }
    }
}
