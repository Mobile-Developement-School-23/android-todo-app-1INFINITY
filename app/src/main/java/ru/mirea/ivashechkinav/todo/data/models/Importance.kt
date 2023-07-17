package ru.mirea.ivashechkinav.todo.data.models

enum class Importance {
    LOW,
    COMMON,
    HIGH;

    companion object {
        fun parseImportanceFromNetwork(text: String): Importance {
            return when (text) {
                "low" -> Importance.LOW
                "basic" -> Importance.COMMON
                "important" -> Importance.HIGH
                else ->
                    throw UnsupportedOperationException("Unable to convert String to Importance unknown value: $text")
            }
        }
    }
}
