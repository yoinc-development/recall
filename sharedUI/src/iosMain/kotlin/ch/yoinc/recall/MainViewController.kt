package ch.yoinc.recall

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { IosApp() }

@Composable
private fun IosApp() {
    var recalls by remember { mutableStateOf(listOf<Recall>()) }
    App(
        recalls = recalls,
        onAdd = { recall -> recalls = recalls + recall },
        onDelete = { id -> recalls = recalls.filter { it.id != id } },
    )
}
