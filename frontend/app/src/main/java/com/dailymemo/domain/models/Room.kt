package com.dailymemo.domain.models

import java.time.LocalDateTime

data class Room(
    val id: String,
    val name: String,
    val ownerId: Long,
    val ownerName: String,
    val participants: List<Participant> = emptyList(),
    val isPublic: Boolean = false,
    val roomPassword: String = "0000",
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class Participant(
    val id: Long,
    val name: String,
    val profileImageUrl: String? = null,
    val isOwner: Boolean = false,
    val permission: RoomPermission = RoomPermission.READ_ONLY,
    val joinedAt: LocalDateTime
)
