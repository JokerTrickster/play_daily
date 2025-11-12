-- Migration: Add tokens table for stateful JWT token management
-- Created: 2025-11-11
-- Purpose: Enable token revocation and rotation for security

CREATE TABLE IF NOT EXISTS tokens (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL COMMENT '사용자 ID (FK to users table)',
    access_token VARCHAR(500) NOT NULL COMMENT 'JWT Access Token',
    refresh_token VARCHAR(500) NOT NULL UNIQUE COMMENT 'JWT Refresh Token (unique, single-use)',
    access_token_expires_at TIMESTAMP NOT NULL COMMENT 'Access Token 만료 시간',
    refresh_token_expires_at TIMESTAMP NOT NULL COMMENT 'Refresh Token 만료 시간',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '토큰 생성 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '토큰 업데이트 시간',
    revoked_at TIMESTAMP NULL DEFAULT NULL COMMENT '토큰 무효화 시간 (NULL = active)',

    -- Indexes for performance
    INDEX idx_user_id (user_id),
    INDEX idx_access_token (access_token),
    INDEX idx_refresh_token (refresh_token),
    INDEX idx_expires_at (access_token_expires_at, refresh_token_expires_at),
    INDEX idx_revoked_at (revoked_at),

    -- Foreign key constraint
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='JWT Token Storage for Revocation and Rotation';

-- Rollback script (run separately if needed)
-- DROP TABLE IF EXISTS tokens;
