-- Add location fields to memos table
USE daily_dev;

ALTER TABLE memos
ADD COLUMN IF NOT EXISTS latitude DOUBLE COMMENT '위도',
ADD COLUMN IF NOT EXISTS longitude DOUBLE COMMENT '경도',
ADD COLUMN IF NOT EXISTS location_name VARCHAR(255) COMMENT '위치 이름';

-- Add index for location queries
CREATE INDEX IF NOT EXISTS idx_location ON memos(latitude, longitude);
