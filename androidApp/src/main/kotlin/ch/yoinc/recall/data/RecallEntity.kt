package ch.yoinc.recall.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.yoinc.recall.Day
import ch.yoinc.recall.Recall
import ch.yoinc.recall.RecallTime

@Entity(tableName = "recalls")
data class RecallEntity(
    @PrimaryKey val id: String,
    val text: String,
    val days: String,
    val hour: Int,
    val minute: Int,
    val description: String,
) {
    fun toRecall() = Recall(
        id = id,
        text = text,
        days = days.split("|").filter { it.isNotEmpty() }.map { Day.valueOf(it) }.toSet(),
        time = RecallTime(hour, minute),
        description = description,
    )
}

fun Recall.toEntity() = RecallEntity(
    id = id,
    text = text,
    days = days.joinToString("|") { it.name },
    hour = time?.hour ?: 12,
    minute = time?.minute ?: 0,
    description = description,
)
