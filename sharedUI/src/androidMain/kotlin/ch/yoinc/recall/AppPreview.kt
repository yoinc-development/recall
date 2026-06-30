package ch.yoinc.recall

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun AppPreview() {
    MaterialTheme {
        App(recalls = emptyList(), onAdd = {}, onDelete = {})
    }
}
