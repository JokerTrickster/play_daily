# Database Migrations

This directory contains SQL migration scripts for the play_daily project.

## Migration: Memo Category System (2025-11-06)

**Issue**: [#41 - Database Migration and Category Seeding](https://github.com/JokerTrickster/play_daily/issues/41)

### Overview

This migration replaces the free-text memo `content` field with a structured category system. Users will select from 10 predefined sentiment categories instead of entering free text.

### ⚠️ Breaking Changes

- **Data Loss**: All existing memo content will be **deleted** (truncated)
- **Schema Changes**: `memos.content` column removed, `memos.creation_mode` column added
- **New Tables**: `memo_categories`, `memo_category_selections` created

### Files

- `20251106_memo_category_system.sql` - Forward migration
- `20251106_rollback_memo_category_system.sql` - Rollback script

---

## Running the Migration

### Prerequisites

1. **Backup Database** (Manual - Before running migration)
   ```bash
   mysqldump -u root -p daily_dev > backup_$(date +%Y%m%d).sql
   ```

2. **Database Credentials**
   - Host: `localhost` (local) or `13.203.37.93` (staging/production)
   - Database: `daily_dev`
   - User: `root`
   - Password: (from secure storage)

### Local Environment

```bash
# Navigate to migrations directory
cd backend/migrations

# Run migration
mysql -u root -p daily_dev < 20251106_memo_category_system.sql

# Expected output:
# - Backup created with X records
# - Memos truncated (0 remaining)
# - Tables created successfully
# - 10 categories inserted
# - Validation queries show correct schema
```

### Staging Environment

```bash
# Connect to staging database
mysql -h 13.203.37.93 -u root -p daily_dev < 20251106_memo_category_system.sql
```

### Production Environment

**⚠️ Production Deployment Checklist:**

1. [ ] Notify users of upcoming maintenance window
2. [ ] Create manual database backup
3. [ ] Schedule deployment during low-traffic period
4. [ ] Test migration on staging first
5. [ ] Have rollback plan ready
6. [ ] Monitor logs after deployment

```bash
# Production execution
mysql -h <production_host> -u root -p daily_prod < 20251106_memo_category_system.sql
```

---

## Validation

After running the migration, verify:

```sql
-- Check memos table structure
DESCRIBE memos;
-- Should show: no 'content' column, has 'creation_mode' column

-- Check new tables exist
SHOW TABLES LIKE 'memo_categories';
SHOW TABLES LIKE 'memo_category_selections';

-- Check categories were seeded
SELECT COUNT(*) FROM memo_categories;
-- Should return: 10

-- View all categories
SELECT id, name_ko, sentiment, display_order, color_hex
FROM memo_categories
ORDER BY display_order;

-- Check foreign keys
SELECT
    CONSTRAINT_NAME,
    TABLE_NAME,
    REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'memo_category_selections'
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- Verify memos table is empty
SELECT COUNT(*) FROM memos;
-- Should return: 0

-- Verify backup table exists
SELECT COUNT(*) FROM memos_backup_20251106;
-- Should return: previous memo count
```

---

## Rollback

If you need to rollback the migration:

```bash
# Run rollback script
mysql -u root -p daily_dev < 20251106_rollback_memo_category_system.sql
```

**Note**: The rollback script:
- ✅ Drops new tables (`memo_categories`, `memo_category_selections`)
- ✅ Restores old schema (`content` column back)
- ❌ Does NOT automatically restore memo data (you must uncomment restoration block)

To restore data during rollback, edit `20251106_rollback_memo_category_system.sql` and uncomment the data restoration block before running.

---

## Testing

### Test on Local Database

1. **Create test database:**
   ```sql
   CREATE DATABASE test_migration;
   USE test_migration;

   -- Create minimal memos table for testing
   CREATE TABLE memos (
       id INT PRIMARY KEY AUTO_INCREMENT,
       user_id INT NOT NULL,
       title VARCHAR(200) NOT NULL,
       content TEXT,
       image_url VARCHAR(255),
       rating INT,
       latitude DOUBLE,
       longitude DOUBLE,
       naver_place_url VARCHAR(255),
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   );

   -- Insert test data
   INSERT INTO memos (user_id, title, content, rating) VALUES
   (1, 'Test Cafe', 'Great coffee and atmosphere', 5),
   (1, 'Test Restaurant', 'Delicious food', 4);
   ```

2. **Run migration:**
   ```bash
   mysql -u root -p test_migration < 20251106_memo_category_system.sql
   ```

3. **Verify results** (see Validation section above)

4. **Test rollback:**
   ```bash
   mysql -u root -p test_migration < 20251106_rollback_memo_category_system.sql
   ```

5. **Clean up:**
   ```sql
   DROP DATABASE test_migration;
   ```

---

## Troubleshooting

### Error: "Table 'memos' doesn't exist"

**Solution**: You're running the migration on the wrong database. Check your connection:
```sql
SELECT DATABASE();
```

### Error: "Duplicate entry for key 'uk_name_ko'"

**Solution**: Categories already exist. This migration was already run. To re-run:
```sql
TRUNCATE TABLE memo_categories;
-- Then re-run migration
```

### Error: "Cannot drop column 'content': check that it exists"

**Solution**: Column was already dropped. Check if migration ran partially. Verify with:
```sql
DESCRIBE memos;
```

### Migration hangs or takes too long

**Possible causes**:
- Large number of existing memos (backup creation is slow)
- Database locks (other processes using the table)

**Solution**: Run during maintenance window when no users are active.

---

## Impact Analysis

### Before Migration
```
memos table:
├── id
├── user_id
├── title
├── content (TEXT) ← REMOVED
├── image_url
├── rating
├── latitude
├── longitude
├── naver_place_url
├── created_at
└── updated_at
```

### After Migration
```
memos table:
├── id
├── user_id
├── title
├── creation_mode (ENUM) ← ADDED
├── image_url
├── rating
├── latitude
├── longitude
├── naver_place_url
├── created_at
└── updated_at

memo_categories table: (NEW)
├── id
├── name_ko
├── sentiment (positive/negative/neutral)
├── display_order
├── color_hex
├── created_at
└── updated_at

memo_category_selections table: (NEW)
├── id
├── memo_id (FK → memos.id)
├── category_id (FK → memo_categories.id)
└── created_at
```

---

## Next Steps

After successful migration:

1. Update backend API to handle category_ids (see Issue #42, #43)
2. Update Android app to use category selection UI (see Issue #44-48)
3. Test end-to-end flows (see Issue #49)

---

## Support

For questions or issues:
- GitHub Issue: https://github.com/JokerTrickster/play_daily/issues/41
- Backend Team: Check team documentation for contact info
