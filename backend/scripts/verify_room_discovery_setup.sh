#!/bin/bash

# Verification script for Issue #60 - Room Discovery Database Setup
# This script verifies that all database changes were applied correctly

set -e

echo "=========================================="
echo "Room Discovery Database Setup Verification"
echo "=========================================="
echo ""

# Database connection details
DB_HOST="${DB_HOST:-13.203.37.93}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-examplepassword}"
DB_NAME="${DB_NAME:-daily_dev}"

# Function to run SQL query
run_query() {
    docker run -i --rm mysql:8.0 mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "$1" 2>&1 | grep -v "Warning"
}

echo "1. Checking Room Discovery Indices..."
echo "--------------------------------------"
INDICES=$(run_query "SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ', ') as columns FROM information_schema.statistics WHERE table_schema = '$DB_NAME' AND table_name = 'rooms' AND index_name LIKE 'idx_rooms_%' GROUP BY index_name ORDER BY index_name;")
echo "$INDICES"
echo ""

# Count indices
INDEX_COUNT=$(echo "$INDICES" | grep -c "idx_rooms_" || true)
if [ "$INDEX_COUNT" -eq 3 ]; then
    echo "✓ All 3 room discovery indices found"
else
    echo "✗ Expected 3 indices, found $INDEX_COUNT"
    exit 1
fi
echo ""

echo "2. Checking Room Password Reset Audit Table..."
echo "------------------------------------------------"
TABLE_EXISTS=$(run_query "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_NAME' AND table_name = 'room_password_resets';")
TABLE_COUNT=$(echo "$TABLE_EXISTS" | tail -1)
if [ "$TABLE_COUNT" -eq 1 ]; then
    echo "✓ Audit table 'room_password_resets' exists"
else
    echo "✗ Audit table not found"
    exit 1
fi
echo ""

echo "3. Verifying Audit Table Schema..."
echo "------------------------------------"
COLUMNS=$(run_query "SELECT column_name, column_type, is_nullable, column_key FROM information_schema.columns WHERE table_schema = '$DB_NAME' AND table_name = 'room_password_resets' ORDER BY ordinal_position;")
echo "$COLUMNS"
echo ""

# Check required columns
REQUIRED_COLS=("id" "room_id" "reset_by_user_id" "reset_at" "previous_password_hash" "new_password_hash")
for col in "${REQUIRED_COLS[@]}"; do
    if echo "$COLUMNS" | grep -q "$col"; then
        echo "✓ Column '$col' exists"
    else
        echo "✗ Column '$col' missing"
        exit 1
    fi
done
echo ""

echo "4. Verifying Foreign Keys..."
echo "------------------------------"
FKS=$(run_query "SELECT constraint_name, referenced_table_name, delete_rule FROM information_schema.referential_constraints WHERE constraint_schema = '$DB_NAME' AND table_name = 'room_password_resets';")
echo "$FKS"
echo ""

FK_COUNT=$(echo "$FKS" | grep -c "fk_room_password_resets" || true)
if [ "$FK_COUNT" -eq 2 ]; then
    echo "✓ Both foreign key constraints exist"
else
    echo "✗ Expected 2 foreign keys, found $FK_COUNT"
    exit 1
fi
echo ""

echo "5. Testing Query Performance..."
echo "--------------------------------"
echo "Popular rooms query EXPLAIN:"
run_query "EXPLAIN SELECT * FROM rooms WHERE is_public = 1 AND deleted_at IS NULL ORDER BY likes_count DESC, created_at DESC LIMIT 20;" | head -5
echo ""

echo "Recent rooms query EXPLAIN:"
run_query "EXPLAIN SELECT * FROM rooms WHERE is_public = 1 AND deleted_at IS NULL ORDER BY created_at DESC, likes_count DESC LIMIT 20;" | head -5
echo ""

echo "6. Database Statistics..."
echo "--------------------------"
STATS=$(run_query "SELECT COUNT(*) as total_rooms, COUNT(CASE WHEN is_public = 1 THEN 1 END) as public_rooms, MAX(likes_count) as max_likes FROM rooms WHERE deleted_at IS NULL;")
echo "$STATS"
echo ""

echo "=========================================="
echo "✓ All verification checks passed!"
echo "=========================================="
echo ""
echo "Summary:"
echo "- 3 composite indices created on rooms table"
echo "- room_password_resets audit table created"
echo "- All foreign key constraints verified"
echo "- Query optimization indices ready"
echo ""
echo "The database is ready for room discovery features!"
