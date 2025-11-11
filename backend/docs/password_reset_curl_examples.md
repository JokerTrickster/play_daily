# Room Password Reset API - cURL Testing Examples

## Prerequisites
1. Obtain a JWT token by logging in
2. Know your room ID (default room created at signup)
3. Ensure you're the room owner

---

## 1. Successful Password Reset

```bash
curl -X POST http://localhost:8080/v0.1/rooms/1/reset-password \
  -H "Content-Type: application/json" \
  -H "tkn: YOUR_JWT_TOKEN_HERE" \
  -v
```

**Expected Response (200 OK)**:
```json
{
  "success": true,
  "new_password": "aB3!xY7@zK2M",
  "message": "Password reset successfully"
}
```

---

## 2. Rate Limiting Test (After 3 Resets)

```bash
# First reset
curl -X POST http://localhost:8080/v0.1/rooms/1/reset-password \
  -H "tkn: YOUR_JWT_TOKEN_HERE"

# Second reset
curl -X POST http://localhost:8080/v0.1/rooms/1/reset-password \
  -H "tkn: YOUR_JWT_TOKEN_HERE"

# Third reset
curl -X POST http://localhost:8080/v0.1/rooms/1/reset-password \
  -H "tkn: YOUR_JWT_TOKEN_HERE"

# Fourth reset (should fail with 429)
curl -X POST http://localhost:8080/v0.1/rooms/1/reset-password \
  -H "tkn: YOUR_JWT_TOKEN_HERE" \
  -v
```

**Expected Response (429 Too Many Requests)**:
```
HTTP/1.1 429 Too Many Requests
Retry-After: 86400

{
  "error": "rate limit exceeded: maximum 3 resets per 24 hours"
}
```

---

## 3. Unauthorized Access (Wrong Owner)

```bash
# Try to reset another user's room
curl -X POST http://localhost:8080/v0.1/rooms/999/reset-password \
  -H "tkn: YOUR_JWT_TOKEN_HERE" \
  -v
```

**Expected Response (403 Forbidden)**:
```json
{
  "error": "only room owner can reset password"
}
```

---

## 4. Non-existent Room

```bash
curl -X POST http://localhost:8080/v0.1/rooms/999999/reset-password \
  -H "tkn: YOUR_JWT_TOKEN_HERE" \
  -v
```

**Expected Response (404 Not Found)**:
```json
{
  "error": "room not found"
}
```

---

## 5. Invalid Room ID Format

```bash
curl -X POST http://localhost:8080/v0.1/rooms/invalid/reset-password \
  -H "tkn: YOUR_JWT_TOKEN_HERE" \
  -v
```

**Expected Response (400 Bad Request)**:
```json
{
  "error": "invalid room_id"
}
```

---

## 6. Missing Authentication Token

```bash
curl -X POST http://localhost:8080/v0.1/rooms/1/reset-password \
  -v
```

**Expected Response (401 Unauthorized)**:
```json
{
  "error": "unauthorized"
}
```

---

## 7. Test New Password Works (Join Room)

```bash
# Step 1: Reset password and save the new password
RESPONSE=$(curl -s -X POST http://localhost:8080/v0.1/rooms/1/reset-password \
  -H "tkn: YOUR_JWT_TOKEN_HERE")

NEW_PASSWORD=$(echo $RESPONSE | jq -r '.new_password')
echo "New Password: $NEW_PASSWORD"

# Step 2: Create a new user
curl -X POST http://localhost:8080/v0.1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "account_id": "testuser_join_'$(date +%s)'",
    "password": "testpass123"
  }'

# Save the new user's token
NEW_USER_TOKEN="<token_from_signup_response>"

# Step 3: Join the room with new password
curl -X POST http://localhost:8080/v0.1/room/join \
  -H "Content-Type: application/json" \
  -H "tkn: $NEW_USER_TOKEN" \
  -d '{
    "room_id": 1,
    "room_password": "'$NEW_PASSWORD'"
  }'
```

**Expected Response (200 OK)**:
```json
{
  "id": 1,
  "owner_id": 1,
  "room_password": ""
}
```

---

## 8. Verify Password Characteristics

```bash
# Get new password and verify it meets requirements
curl -s -X POST http://localhost:8080/v0.1/rooms/1/reset-password \
  -H "tkn: YOUR_JWT_TOKEN_HERE" | jq -r '.new_password' | \
  while read pwd; do
    echo "Password: $pwd"
    echo "Length: ${#pwd}"
    echo "Has uppercase: $(echo $pwd | grep -q '[A-Z]' && echo 'Yes' || echo 'No')"
    echo "Has lowercase: $(echo $pwd | grep -q '[a-z]' && echo 'Yes' || echo 'No')"
    echo "Has digit: $(echo $pwd | grep -q '[0-9]' && echo 'Yes' || echo 'No')"
    echo "Has special: $(echo $pwd | grep -q '[!@#$%^&*]' && echo 'Yes' || echo 'No')"
  done
```

**Expected Output**:
```
Password: aB3!xY7@zK2M
Length: 12
Has uppercase: Yes
Has lowercase: Yes
Has digit: Yes
Has special: Yes
```

---

## 9. Check Audit Logs (Database Query)

```sql
-- View all password resets for a room
SELECT
  id,
  room_id,
  reset_by_user_id,
  reset_at,
  previous_password_hash,
  new_password_hash,
  ip_address,
  user_agent
FROM room_password_resets
WHERE room_id = 1
ORDER BY reset_at DESC;

-- Check rate limit violations
SELECT * FROM room_password_resets
WHERE previous_password_hash = 'RATE_LIMIT_VIOLATION'
ORDER BY reset_at DESC;

-- Count resets in last 24 hours for a room
SELECT COUNT(*) as reset_count
FROM room_password_resets
WHERE room_id = 1
  AND reset_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
  AND previous_password_hash != 'RATE_LIMIT_VIOLATION';
```

---

## 10. Complete Integration Test Script

```bash
#!/bin/bash

# Configuration
BASE_URL="http://localhost:8080"
ACCOUNT_ID="testuser_$(date +%s)"
PASSWORD="testpass123"

echo "=== Room Password Reset Integration Test ==="

# 1. Sign up
echo -e "\n1. Creating user..."
SIGNUP_RESPONSE=$(curl -s -X POST "$BASE_URL/v0.1/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "account_id": "'$ACCOUNT_ID'",
    "password": "'$PASSWORD'"
  }')

TOKEN=$(echo $SIGNUP_RESPONSE | jq -r '.access_token')
ROOM_ID=$(echo $SIGNUP_RESPONSE | jq -r '.default_room_id')

echo "Token: $TOKEN"
echo "Room ID: $ROOM_ID"

# 2. First reset
echo -e "\n2. First password reset..."
RESET1=$(curl -s -X POST "$BASE_URL/v0.1/rooms/$ROOM_ID/reset-password" \
  -H "tkn: $TOKEN")
PWD1=$(echo $RESET1 | jq -r '.new_password')
echo "Password 1: $PWD1"

# 3. Second reset
echo -e "\n3. Second password reset..."
sleep 1
RESET2=$(curl -s -X POST "$BASE_URL/v0.1/rooms/$ROOM_ID/reset-password" \
  -H "tkn: $TOKEN")
PWD2=$(echo $RESET2 | jq -r '.new_password')
echo "Password 2: $PWD2"

# 4. Third reset
echo -e "\n4. Third password reset..."
sleep 1
RESET3=$(curl -s -X POST "$BASE_URL/v0.1/rooms/$ROOM_ID/reset-password" \
  -H "tkn: $TOKEN")
PWD3=$(echo $RESET3 | jq -r '.new_password')
echo "Password 3: $PWD3"

# 5. Fourth reset (should fail)
echo -e "\n5. Fourth password reset (should fail with 429)..."
RESET4=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "$BASE_URL/v0.1/rooms/$ROOM_ID/reset-password" \
  -H "tkn: $TOKEN")
echo "$RESET4"

# 6. Verify password uniqueness
echo -e "\n6. Verifying password uniqueness..."
echo "Password 1: $PWD1"
echo "Password 2: $PWD2"
echo "Password 3: $PWD3"

if [ "$PWD1" != "$PWD2" ] && [ "$PWD2" != "$PWD3" ] && [ "$PWD1" != "$PWD3" ]; then
  echo "✅ All passwords are unique"
else
  echo "❌ Passwords are not unique!"
fi

# 7. Verify password requirements
echo -e "\n7. Verifying password requirements for Password 3..."
if [[ ${#PWD3} -eq 12 ]]; then
  echo "✅ Length: 12 characters"
else
  echo "❌ Length: ${#PWD3} (expected 12)"
fi

if [[ $PWD3 =~ [A-Z] ]]; then
  echo "✅ Has uppercase"
else
  echo "❌ Missing uppercase"
fi

if [[ $PWD3 =~ [a-z] ]]; then
  echo "✅ Has lowercase"
else
  echo "❌ Missing lowercase"
fi

if [[ $PWD3 =~ [0-9] ]]; then
  echo "✅ Has digit"
else
  echo "❌ Missing digit"
fi

if [[ $PWD3 =~ [!@#\$%^&*] ]]; then
  echo "✅ Has special character"
else
  echo "❌ Missing special character"
fi

echo -e "\n=== Test Complete ==="
```

**Save as**: `test_password_reset.sh`
**Make executable**: `chmod +x test_password_reset.sh`
**Run**: `./test_password_reset.sh`

---

## Tips for Testing

1. **Use jq for JSON parsing**: Install with `brew install jq` (macOS) or `apt-get install jq` (Linux)

2. **Save tokens in variables**:
   ```bash
   export TEST_TOKEN="your_jwt_token_here"
   curl -H "tkn: $TEST_TOKEN" ...
   ```

3. **Use Postman for interactive testing**:
   - Import collection with all endpoints
   - Use environment variables for tokens
   - View formatted JSON responses

4. **Check server logs**: Watch for errors during password generation

5. **Verify database**: Check `room_password_resets` table after each reset

6. **Test rate limiting**: Wait 24 hours or clear database records to reset counter
