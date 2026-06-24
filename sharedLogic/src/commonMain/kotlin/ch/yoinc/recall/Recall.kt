package ch.yoinc.recall

data class RecallTime(val hour: Int, val minute: Int) {
    override fun toString(): String =
        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

data class Recall(
    val id: String,
    val text: String,
    val days: Set<Day>,
    val time: RecallTime? = null,
    val description: String = "",
)

enum class Day(val label: String) {
    MONDAY("Mo"),
    TUESDAY("Tu"),
    WEDNESDAY("We"),
    THURSDAY("Th"),
    FRIDAY("Fr"),
    SATURDAY("Sa"),
    SUNDAY("Su"),
}
