package com.dailymemo.domain.models

data class RoomMember(
    val userId: Long,
    val userName: String,
    val email: String,
    val profileImageUrl: String?,
    val permission: RoomPermission,
    val joinedAt: Long
)

enum class RoomPermission {
    OWNER,
    READ_WRITE,
    READ_ONLY;

    companion object {
        fun fromString(value: String): RoomPermission {
            return when (value.uppercase()) {
                "OWNER" -> OWNER
                "READ_WRITE" -> READ_WRITE
                "READ_ONLY" -> READ_ONLY
                else -> READ_ONLY
            }
        }
    }
}
