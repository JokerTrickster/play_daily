# Bio Feature - Quick Reference Guide

## Migration Command
```bash
# Apply migration
mysql -u root -p daily_dev < src/common/db/mysql/migration_add_bio_field.sql

# Verify column was added
mysql -u root -p -e "USE daily_dev; DESCRIBE users;" | grep bio
```

## API Examples

### 1. Get Profile (includes bio)
```bash
curl -X GET http://localhost:8080/v0.1/profile \
  -H "Authorization: Bearer <token>"
```

**Response:**
```json
{
  "user_id": 1,
  "account_id": "user@example.com",
  "nickname": "John Doe",
  "profile_image_url": "https://example.com/image.jpg",
  "bio": "Software engineer passionate about Go and APIs",
  "default_room_id": 1,
  "room_password": "1234",
  "received_likes_count": 5
}
```

### 2. Update Profile (with bio)
```bash
curl -X PUT http://localhost:8080/v0.1/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "current_password": "mypassword",
    "bio": "Updated bio - Love building scalable systems"
  }'
```

### 3. Update Profile (clear bio)
```bash
curl -X PUT http://localhost:8080/v0.1/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "current_password": "mypassword",
    "bio": ""
  }'
```

### 4. Get Room Members (includes bio)
```bash
curl -X GET http://localhost:8080/v0.1/rooms/1/members \
  -H "Authorization: Bearer <token>"
```

**Response:**
```json
{
  "room_id": 1,
  "members": [
    {
      "user_id": 1,
      "user_name": "John Doe",
      "email": "john@example.com",
      "profile_image_url": "https://example.com/john.jpg",
      "bio": "Team lead and architect",
      "permission": "OWNER",
      "joined_at": 1699999999
    },
    {
      "user_id": 2,
      "user_name": "Jane Smith",
      "email": "jane@example.com",
      "profile_image_url": "https://example.com/jane.jpg",
      "bio": "Backend developer specializing in Go",
      "permission": "READ_WRITE",
      "joined_at": 1699999999
    }
  ]
}
```

## Direct SQL Testing

### Insert user with bio
```sql
USE daily_dev;

UPDATE users
SET bio = 'This is my bio'
WHERE id = 1;
```

### Query users with bio
```sql
USE daily_dev;

SELECT id, account_id, nickname, bio
FROM users
WHERE bio IS NOT NULL;
```

### Test character limit (should succeed)
```sql
USE daily_dev;

UPDATE users
SET bio = REPEAT('A', 500)
WHERE id = 1;
```

### Test character limit exceeded (should fail)
```sql
USE daily_dev;

UPDATE users
SET bio = REPEAT('A', 501)
WHERE id = 1;
-- Expected: Data too long for column 'bio'
```

## Testing Checklist

### Database Tests
- [x] Migration applies without errors
- [x] Column `bio` exists in `users` table
- [x] Column type is VARCHAR(500)
- [x] Column allows NULL values
- [x] Column positioned after `profile_image_url`

### API Tests - Profile
- [ ] GET /v0.1/profile returns `bio` field (null for existing users)
- [ ] PUT /v0.1/profile with bio updates the field
- [ ] PUT /v0.1/profile without bio leaves field unchanged
- [ ] PUT /v0.1/profile with empty bio clears the field
- [ ] PUT /v0.1/profile with 500 char bio succeeds
- [ ] Bio field returned matches stored value

### API Tests - Room Members
- [ ] GET /v0.1/rooms/{id}/members includes bio for all members
- [ ] Empty bio shows as empty string (not null)
- [ ] Bio field displayed correctly for multiple members

### Edge Cases
- [ ] User without bio (existing users): returns null or empty string
- [ ] User with NULL bio: returns null or empty string
- [ ] User with empty string bio: returns empty string
- [ ] Special characters in bio (emoji, unicode): stored and retrieved correctly
- [ ] Very long bio (500 chars): stored and retrieved correctly

## Common Issues & Solutions

### Issue: Migration fails with "Column already exists"
**Solution:** Column was already added. Check with:
```sql
DESCRIBE users;
```
If needed, drop and re-add:
```sql
ALTER TABLE users DROP COLUMN bio;
-- Then run migration again
```

### Issue: API returns null instead of empty string
**Behavior:** This is expected for Go `*string` types
- `nil` pointer → `null` in JSON
- Empty string → `""` in JSON
**Solution:** Frontend should handle both null and empty string

### Issue: Bio truncated to 500 characters
**Behavior:** This is expected database constraint
**Solution:**
- Add frontend validation to prevent submission > 500 chars
- Or update VARCHAR(500) to TEXT type (not recommended)

### Issue: Room members bio field missing
**Check:** Ensure User preload in getRoomMembersRepository.go:
```go
Preload("User")
```

## File Locations

### Modified Files
```
backend/src/common/db/mysql/
├── gormDB.go                                    # User model with bio
└── migration_add_bio_field.sql                  # Migration script

backend/src/features/profile/
├── model/
│   ├── request/updateProfile.go                 # Request DTO
│   ├── response/profile.go                      # Response DTO
│   └── interface/IProfileRepository.go          # Repository interface
├── repository/
│   └── updateProfileRepository.go               # Update implementation
└── usecase/
    ├── getProfileUseCase.go                     # Get use case
    └── updateProfileUseCase.go                  # Update use case

backend/src/features/room/
├── model/response/roomMember.go                 # Room member DTO
└── repository/getRoomMembersRepository.go       # Room members implementation
```

## Build Verification
```bash
cd /Users/luxrobo/project/play_daily/backend/src
go build
# Should complete without errors
```

## Rollback Instructions

### Database Rollback
```sql
USE daily_dev;
ALTER TABLE users DROP COLUMN bio;
```

### Code Rollback
Use git to revert changes:
```bash
git checkout HEAD -- src/common/db/mysql/gormDB.go
git checkout HEAD -- src/features/profile/
git checkout HEAD -- src/features/room/model/response/roomMember.go
git checkout HEAD -- src/features/room/repository/getRoomMembersRepository.go
git rm src/common/db/mysql/migration_add_bio_field.sql
```
