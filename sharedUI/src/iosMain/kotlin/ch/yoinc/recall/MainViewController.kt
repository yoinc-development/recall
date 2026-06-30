package ch.yoinc.recall

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.flow.MutableStateFlow

// Flat primitive-only DTO – crosses the Swift/Kotlin bridge cleanly.
class RecallInfo(
    val id: String,
    val text: String,
    val body: String,
    val dayNames: String, // pipe-separated: "MONDAY|WEDNESDAY|FRIDAY"
    val hour: Int,
    val minute: Int,
)

interface IosRecallListener {
    fun onRecallAdded(info: RecallInfo)
    fun onRecallDeleted(id: String)
}

// Held by Swift; call openRecall to navigate into a specific recall from a notification tap.
class IosAppState {
    internal val notificationRecallId = MutableStateFlow<String?>(null)

    fun openRecall(id: String) {
        notificationRecallId.value = id
    }
}

fun MainViewController(
    initialRecalls: List<RecallInfo>,
    appState: IosAppState,
    listener: IosRecallListener?,
) = ComposeUIViewController {
    val notificationRecallId by appState.notificationRecallId.collectAsState()
    MaterialTheme {
        IosApp(initialRecalls, notificationRecallId, listener)
    }
}

@Composable
private fun IosApp(
    initialRecalls: List<RecallInfo>,
    initialRecallId: String?,
    listener: IosRecallListener?,
) {
    var recalls by remember { mutableStateOf(initialRecalls.map { it.toRecall() }) }
    App(
        recalls = recalls,
        onAdd = { recall ->
            recalls = recalls + recall
            listener?.onRecallAdded(recall.toInfo())
        },
        onDelete = { id ->
            recalls = recalls.filter { it.id != id }
            listener?.onRecallDeleted(id)
        },
        initialRecallId = initialRecallId,
    )
}

internal fun RecallInfo.toRecall() = Recall(
    id = id,
    text = text,
    days = dayNames.split("|").filter { it.isNotEmpty() }
        .mapNotNull { runCatching { Day.valueOf(it) }.getOrNull() }.toSet(),
    time = RecallTime(hour, minute),
    description = body,
)

internal fun Recall.toInfo() = RecallInfo(
    id = id,
    text = text,
    body = description,
    dayNames = days.joinToString("|") { it.name },
    hour = time?.hour ?: 12,
    minute = time?.minute ?: 0,
)
