package com.dailymemo.domain.models

import java.time.LocalDateTime

data class Room(
    val id: String,
    val name: String,
    val ownerId: Long,
    val ownerName: String,
    val participants: List<Participant> = emptyList(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class Participant(
    val id: Long,
    val name: String,
    val isOwner: Boolean = false,
    val joinedAt: LocalDateTime
)
