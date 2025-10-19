-- play_daily Database Initialization Script
-- 데이터베이스가 없으면 생성
CREATE DATABASE IF NOT EXISTS daily_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE daily_dev;

-- Users Table: 유저 정보 관리
CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    account_id VARCHAR(255) NOT NULL UNIQUE COMMENT '계정 아이디',
    password VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
    nickname VARCHAR(100) COMMENT '사용자 닉네임',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    deleted_at TIMESTAMP NULL DEFAULT NULL COMMENT '삭제 시간 (soft delete)',
    INDEX idx_account_id (account_id),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 정보 테이블';

-- Memos Table: 메모 정보 관리
CREATE TABLE IF NOT EXISTS memos (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL COMMENT '작성자 ID',
    title VARCHAR(200) NOT NULL COMMENT '메모 제목',
    content TEXT COMMENT '메모 내용',
    image_url VARCHAR(500) COMMENT '메모 이미지 URL',
    rating TINYINT UNSIGNED DEFAULT 0 COMMENT '평점 (0-5)',
    is_pinned BOOLEAN DEFAULT FALSE COMMENT '고정 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    deleted_at TIMESTAMP NULL DEFAULT NULL COMMENT '삭제 시간 (soft delete)',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_deleted_at (deleted_at),
    INDEX idx_is_pinned (is_pinned),
    INDEX idx_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='메모 정보 테이블';

-- Test User Data
INSERT INTO users (account_id, password, nickname)
VALUES ('jhj485', '456123', 'jhj485')
ON DUPLICATE KEY UPDATE account_id=account_id;


