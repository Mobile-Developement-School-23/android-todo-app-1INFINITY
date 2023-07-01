package ru.mirea.ivashechkinav.todo.data.models

enum class Importance {
    LOW,
    COMMON,
    HIGH
}

fun parseImportanceFromNetwork(text: String): Importance {
    return when (text) {
        "low" -> Importance.LOW
        "basic" -> Importance.COMMON
        "important" -> Importance.HIGH
        else -> throw UnsupportedOperationException("Unable to convert String to Importance unknown value: $text")
    }
}

fun Importance.toNetworkFormat(): String {
    return when (this) {
        Importance.LOW -> "low"
        Importance.COMMON -> "basic"
        Importance.HIGH -> "important"
        else -> throw UnsupportedOperationException("Unknown Importance value: $this")
    }
}