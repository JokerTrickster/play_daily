-- Test user for login: jhj485 / 456123

INSERT INTO users (account_id, password, nickname, created_at, updated_at)
VALUES ('jhj485', '456123', 'jhj485', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    password = '456123',
    updated_at = NOW();
