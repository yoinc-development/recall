package ch.yoinc.recall

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun App(
    recalls: List<Recall>,
    onAdd: (Recall) -> Unit,
    onDelete: (String) -> Unit,
    initialRecallId: String? = null,
) {
    RecallList(
        recalls = recalls,
        onAdd = onAdd,
        onDelete = onDelete,
        initialRecallId = initialRecallId,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecallList(
    recalls: List<Recall>,
    onAdd: (Recall) -> Unit,
    onDelete: (String) -> Unit,
    initialRecallId: String? = null,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var selectedRecall by remember { mutableStateOf<Recall?>(null) }

    LaunchedEffect(initialRecallId, recalls) {
        if (initialRecallId != null && selectedRecall == null) {
            selectedRecall = recalls.find { it.id == initialRecallId }
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Recalls") },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text(
                    text = "+",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        },
    ) { innerPadding ->
        if (recalls.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(32.dp),
                ) {
                    Text(text = "🔔", fontSize = 48.sp)
                    Text(
                        text = "No recalls yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Tap + to add something you want to be reminded of.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                items(recalls, key = { it.id }) { recall ->
                    SwipeToDeleteItem(
                        recall = recall,
                        isPendingDelete = pendingDeleteId == recall.id,
                        onSwiped = { pendingDeleteId = recall.id },
                        onConfirmDelete = {
                            onDelete(recall.id)
                            pendingDeleteId = null
                        },
                        onCancelDelete = { pendingDeleteId = null },
                        onClick = { selectedRecall = recall },
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showAddDialog) {
        AddRecallDialog(
            onConfirm = { text, days, time, description ->
                onAdd(
                    Recall(
                        id = kotlin.random.Random.nextLong().toString(),
                        text = text,
                        days = days,
                        time = time,
                        description = description,
                    )
                )
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }

    selectedRecall?.let { recall ->
        RecallDetailSheet(
            recall = recall,
            onDismiss = { selectedRecall = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
            Text(text = "🗑", fontSize = 24.sp)
            Row {
                TextButton(onClick = onCancelDelete) { Text("Cancel") }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecallDetailSheet(
    recall: Recall,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = recall.text,
                style = MaterialTheme.typography.headlineSmall,
            )
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
                                else MaterialTheme.colorScheme.surfaceVariant,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (recall.description.isNotBlank()) {
                HorizontalDivider()
                Text(
                    text = recall.description,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
