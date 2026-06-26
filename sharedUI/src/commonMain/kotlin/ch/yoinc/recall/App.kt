package ch.yoinc.recall

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun App() {
    MaterialTheme {
        RecallList()
    }
}

@Composable
fun RecallList() {
    var recalls by remember { mutableStateOf(listOf<Recall>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var selectedRecall by remember { mutableStateOf<Recall?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding(),
    ) {
        if (recalls.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "No recalls added. Look at you, remembering everything.",
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = Color(0xFF1565C0),
                                textDecoration = TextDecoration.Underline,
                            )
                        ) {
                            append("Add a Recall")
                        }
                    },
                    modifier = Modifier.clickable { showAddDialog = true },
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(recalls, key = { it.id }) { recall ->
                        SwipeToDeleteItem(
                            recall = recall,
                            isPendingDelete = pendingDeleteId == recall.id,
                            onSwiped = { pendingDeleteId = recall.id },
                            onConfirmDelete = {
                                recalls = recalls.filter { it.id != recall.id }
                                pendingDeleteId = null
                            },
                            onCancelDelete = { pendingDeleteId = null },
                            onClick = { selectedRecall = recall },
                        )
                        HorizontalDivider()
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { showAddDialog = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "+",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 24.sp,
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddRecallDialog(
            onConfirm = { text, days, time, description ->
                recalls = recalls + Recall(
                    id = kotlin.random.Random.nextLong().toString(),
                    text = text,
                    days = days,
                    time = time,
                    description = description,
                )
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }

    selectedRecall?.let { recall ->
        RecallDetailDialog(
            recall = recall,
            onDismiss = { selectedRecall = null },
        )
    }
}

@Composable
private fun SwipeToDeleteItem(
    recall: Recall,
    isPendingDelete: Boolean,
    onSwiped: () -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onClick: () -> Unit,
) {
    if (isPendingDelete) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "🗑",
                fontSize = 24.sp,
            )
            Row {
                TextButton(onClick = onCancelDelete) {
                    Text("Cancel")
                }
                TextButton(onClick = onConfirmDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    } else {
        val state = rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == SwipeToDismissBoxValue.EndToStart) {
                    onSwiped()
                    true
                } else {
                    false
                }
            },
        )
        SwipeToDismissBox(
            state = state,
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Text(
                        text = "🗑",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 20.dp),
                    )
                }
            },
        ) {
            ListItem(
                headlineContent = { Text(recall.text) },
                supportingContent = run {
                    val days = recall.days.joinToString(" ") { it.label }
                    val time = recall.time?.toString()
                    val subtitle = listOfNotNull(days.ifEmpty { null }, time).joinToString("  ")
                    if (subtitle.isNotEmpty()) {
                        { Text(subtitle) }
                    } else {
                        null
                    }
                },
                modifier = Modifier.clickable { onClick() },
            )
        }
    }
}

@Composable
private fun RecallDetailDialog(
    recall: Recall,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(recall.text) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Day.entries.forEach { day ->
                        val selected = day in recall.days
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = day.label,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                            )
                        }
                    }
                }
                recall.time?.let { time ->
                    Text(
                        text = time.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                if (recall.description.isNotBlank()) {
                    Text(
                        text = recall.description,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}