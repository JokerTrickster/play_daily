package com.dailymemo.domain.models

enum class CategorySentiment {
    POSITIVE,
    NEGATIVE,
    NEUTRAL;

    companion object {
        fun fromString(value: String): CategorySentiment = when (value.lowercase()) {
            "positive" -> POSITIVE
            "negative" -> NEGATIVE
            "neutral" -> NEUTRAL
            else -> NEUTRAL
        }
    }
}
