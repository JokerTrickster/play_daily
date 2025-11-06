package com.dailymemo.domain.models

enum class CreationMode {
    MAP,
    LIST;

    fun toApiValue(): String = name.lowercase()

    companion object {
        fun fromString(value: String): CreationMode = when (value.lowercase()) {
            "map" -> MAP
            "list" -> LIST
            else -> LIST
        }
    }
}
