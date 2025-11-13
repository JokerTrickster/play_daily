-- migration_rating_to_float.sql
-- Change rating column from tinyint to float to support decimal ratings (e.g., 3.5)

ALTER TABLE memos MODIFY COLUMN rating FLOAT DEFAULT 0 COMMENT '평점 (0-5, 0.5 단위) 또는 관심도 (1-5)';
