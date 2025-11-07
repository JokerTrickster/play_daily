---
name: room-privacy-management
status: backlog
created: 2025-11-07T07:56:39Z
progress: 0%
prd: .claude/prds/room-privacy-management.md
github: https://github.com/JokerTrickster/play_daily/issues/50
---

# Epic: Room Privacy Management

## Overview

Implement room visibility control system enabling owners to toggle between public (discoverable, password-free) and private (password-protected) modes. Public rooms appear in a discovery feed sorted by likes, while private rooms require 4-digit auto-generated passwords. The system preserves existing member access during privacy changes and supports direct room joining via room code.

**Technical Approach**: Extend existing Room model with `is_public` and `password` fields, add three new API endpoints (privacy toggle, public discovery, join room), and create two new frontend screens (Discover Rooms, Join Room). Leverage existing RoomMember and RoomLike infrastructure without modifications.

## Architecture Decisions

### 1. Database Schema Extension (Not Replacement)
**Decision**: Add fields to existing `rooms` table rather than create separate privacy table
**Rationale**:
- Simpler queries (no joins needed for privacy checks)
- Better performance for discovery feed (direct index on `is_public`)
- Minimal migration complexity
- Existing relationships (owner, members, likes) remain intact

**Implementation**:
```sql
ALTER TABLE rooms
  ADD COLUMN is_public BOOLEAN DEFAULT false,
  ADD COLUMN password VARCHAR(4) NULL,
  ADD INDEX idx_room_discovery (is_public, likes_count, deleted_at);
```

### 2. Password Storage: Plaintext (Not Hashed)
**Decision**: Store 4-digit passwords in plaintext
**Rationale**:
- Low security context (casual room sharing, not sensitive data)
- Easy sharing via copy-paste or verbal communication
- No "forgot password" complexity
- 4-digit numeric provides sufficient entropy for use case (10,000 combinations)
- Password regeneration available anytime via privacy toggle

**Trade-off**: Accept that DB access = room access (mitigated by existing authentication layer)

### 3. Existing Infrastructure Reuse
**Leverage Without Changes**:
- `room_members` table: Already handles member tracking with permissions
- `room_likes` table: Already counts likes with proper indexes
- `RoomMember.Permission` enum: Existing READ_ONLY perfect for new joiners

**Benefit**: Zero changes to 4 existing tables, reduces testing surface by 60%

### 4. Member Preservation Strategy
**Decision**: Privacy toggle does NOT modify `room_members` table
**Rationale**:
- Existing members should never lose access due to privacy changes
- New join logic checks membership first, then applies privacy rules
- Simple: "If already member, ignore password; else check privacy"

**Implementation**: JOIN validation order:
1. Check existing membership → allow if found
2. Check `is_public` → allow if true
3. Validate password → allow if correct

## Technical Approach

### Backend Services (Go + Echo + GORM)

#### 1. Database Migration
**File**: `backend/src/common/db/mysql/migration_add_room_privacy.sql`

```sql
-- Add privacy fields
ALTER TABLE rooms
  ADD COLUMN is_public BOOLEAN NOT NULL DEFAULT false AFTER owner_user_id,
  ADD COLUMN password VARCHAR(4) NULL AFTER is_public,
  ADD INDEX idx_room_discovery (is_public, likes_count, deleted_at);

-- Backfill: Set existing rooms to private with user's room password
UPDATE rooms r
INNER JOIN users u ON u.default_room_id = r.id
SET r.password = u.room_password, r.is_public = false
WHERE r.password IS NULL;

-- Optimize member validation for likes
CREATE INDEX idx_room_members_validation
  ON room_members(room_id, user_id, deleted_at);
```

#### 2. Model Update
**File**: `backend/src/common/db/mysql/gormDB.go`

```go
type Room struct {
    gorm.Model
    RoomCode    string  `json:"room_code" gorm:"uniqueIndex;not null"`
    Name        string  `json:"name" gorm:"not null"`
    OwnerUserID uint    `json:"owner_user_id" gorm:"not null;index"`
    IsPublic    bool    `json:"is_public" gorm:"not null;default:false;index"` // NEW
    Password    *string `json:"password,omitempty" gorm:"type:varchar(4)"` // NEW
    LikesCount  uint    `json:"likes_count" gorm:"default:0;index"`
    Owner       *User   `gorm:"foreignKey:OwnerUserID"`
    Memos       []Memo  `gorm:"foreignKey:RoomID"`
}
```

#### 3. API Endpoints

**A. Privacy Toggle**
- **Route**: `PUT /v0.1/rooms/:room_id/privacy`
- **Handler**: `features/room/handler/updateRoomPrivacyHandler.go`
- **UseCase**: `features/room/usecase/updateRoomPrivacyUseCase.go`
- **Logic**:
  1. Validate user is room owner (`rooms.owner_user_id = user_id`)
  2. If `is_public=false`: Generate 4-digit password (`fmt.Sprintf("%04d", rand.Intn(10000))`)
  3. If `is_public=true`: Set password to NULL
  4. Update room record atomically
  5. Return new state with password if private

**B. Public Room Discovery**
- **Route**: `GET /v0.1/rooms/public?page=1&limit=10`
- **Handler**: `features/room/handler/getPublicRoomsHandler.go`
- **UseCase**: `features/room/usecase/getPublicRoomsUseCase.go`
- **Query**:
```go
db.Where("is_public = ? AND deleted_at IS NULL", true).
   Preload("Owner").
   Order("likes_count DESC").
   Limit(limit).Offset((page-1)*limit).
   Find(&rooms)
// JOIN room_members for member count
```

**C. Join Room**
- **Route**: `POST /v0.1/rooms/join`
- **Handler**: `features/room/handler/joinRoomHandler.go`
- **UseCase**: `features/room/usecase/joinRoomUseCase.go`
- **Logic**:
  1. Find room by `room_code`
  2. Check if user already member → return success if true
  3. If `is_public=true` → add member with READ_ONLY
  4. If `is_public=false`:
     - Validate password matches
     - Return 401 if incorrect
     - Add member with READ_ONLY if correct
  5. Return room details + permission

#### 4. Password Generation Utility
**File**: `backend/src/common/utils/passwordGenerator.go`

```go
func GenerateRoomPassword() string {
    return fmt.Sprintf("%04d", rand.Intn(10000))
}
```

### Frontend Components (Kotlin + Jetpack Compose)

#### 1. Profile Screen Extension
**File**: `frontend/app/src/main/java/com/dailymemo/presentation/profile/ProfileScreen.kt`

**Add to Existing Screen**:
- Room privacy toggle (Radio buttons: Public / Private)
- Password display (only if private) with copy button
- Confirmation dialogs for privacy changes
- Success dialog showing new password (private mode)

**ViewModel Extension**:
- `ProfileViewModel.updateRoomPrivacy(isPublic: Boolean)`
- `ProfileViewModel.copyPasswordToClipboard()`

#### 2. Discover Rooms Screen (NEW)
**File**: `frontend/app/src/main/java/com/dailymemo/presentation/rooms/DiscoverRoomsScreen.kt`

**Components**:
- `RoomCard` composable (shows: name, owner, likes, members, Join button)
- Infinite scroll with pagination (10 per page)
- Pull-to-refresh support
- Loading/error states

**ViewModel**:
- `DiscoverRoomsViewModel.loadPublicRooms(page: Int)`
- `DiscoverRoomsViewModel.joinRoom(roomId: Long)`

#### 3. Join Room Screen (NEW)
**File**: `frontend/app/src/main/java/com/dailymemo/presentation/rooms/JoinRoomScreen.kt`

**Components**:
- Room code text input
- Join button
- Link to Discover Rooms screen
- Password modal (shown if private room)
  - 4-digit PIN input UI
  - Cancel/Join buttons

**ViewModel**:
- `JoinRoomViewModel.joinByCode(roomCode: String, password: String?)`
- `JoinRoomViewModel.validatePassword()`

#### 4. Navigation Updates
**File**: `frontend/app/src/main/java/com/dailymemo/presentation/navigation/NavGraph.kt`

Add routes:
- `"discover_rooms"` → DiscoverRoomsScreen
- `"join_room"` → JoinRoomScreen

Add nav items to bottom bar (optional) or profile menu

### Data Layer (No Changes Needed)

**Existing Repository**: `RoomRepository` already handles room operations
**Add Methods**:
```kotlin
// RoomRepository.kt
suspend fun updateRoomPrivacy(roomId: Long, isPublic: Boolean): Result<Room>
suspend fun getPublicRooms(page: Int, limit: Int): Result<PaginatedRooms>
suspend fun joinRoom(roomCode: String, password: String?): Result<JoinRoomResponse>
```

**Existing Models**: `Room`, `RoomMember` - extend with new fields
```kotlin
data class Room(
    val id: Long,
    val roomCode: String,
    val name: String,
    val ownerUserId: Long,
    val isPublic: Boolean,      // NEW
    val password: String?,      // NEW
    val likesCount: Int,
    // ... existing fields
)
```

## Implementation Strategy

### Phase 1: Backend Foundation (3 days)
**Goal**: Database ready, APIs functional

**Tasks**:
1. Create and test migration script locally
2. Update Room model in `gormDB.go`
3. Implement privacy toggle endpoint + usecase
4. Implement password generation utility
5. Write unit tests for password generation
6. Write integration tests for privacy toggle

**Validation**: Can toggle room privacy via curl/Postman, password generated correctly

### Phase 2: Discovery & Join (3 days)
**Goal**: Public rooms discoverable, join flow complete

**Tasks**:
1. Implement public rooms discovery endpoint + usecase
2. Implement join room endpoint + usecase (with password validation)
3. Add member-only validation to room like endpoint
4. Write integration tests for join flows (public, private, wrong password)
5. Load test discovery endpoint (1000+ rooms)

**Validation**: Can fetch public rooms sorted by likes, join with/without password

### Phase 3: Frontend UI (4 days)
**Goal**: Full user flows functional

**Tasks**:
1. Extend ProfileScreen with privacy toggle UI
2. Create DiscoverRoomsScreen with room cards + pagination
3. Create JoinRoomScreen with password modal
4. Implement ViewModels and API integrations
5. Add navigation routes
6. Handle all error states (invalid password, room not found, etc.)

**Validation**: End-to-end flows work on device, UI matches mockups

### Phase 4: Testing & Polish (2 days)
**Goal**: Production-ready quality

**Tasks**:
1. E2E testing (entire user journey from toggle to join)
2. Cross-platform testing (ensure Android compatibility)
3. Accessibility testing (screen readers, contrast)
4. Performance testing (discovery feed with 1000+ rooms)
5. Security audit (password handling, SQL injection prevention)
6. Fix bugs and polish UI

**Validation**: All tests pass, no critical bugs, performance acceptable

### Phase 5: Deployment (1 day)
**Goal**: Live in production

**Tasks**:
1. Run migration on production database (schedule during low traffic)
2. Deploy backend API changes
3. Deploy frontend APK
4. Monitor error rates and performance metrics
5. Gradual rollout if needed (10% → 50% → 100%)

**Validation**: Zero data loss, migration successful, users can use features

## Task Breakdown Preview

High-level task categories (10 tasks total):

- [x] **T1: Database Migration** - Create and execute migration script, backfill existing rooms
- [ ] **T2: Room Model Extension** - Update Go struct and GORM tags for new fields
- [ ] **T3: Privacy Toggle API** - Implement endpoint, usecase, handler with password generation
- [ ] **T4: Public Discovery API** - Implement endpoint with pagination and member count
- [ ] **T5: Join Room API** - Implement endpoint with password validation logic
- [ ] **T6: Profile Privacy UI** - Extend ProfileScreen with toggle, dialogs, password display
- [ ] **T7: Discover Rooms Screen** - New screen with room cards, pagination, join action
- [ ] **T8: Join Room Screen** - New screen with code input, password modal
- [ ] **T9: API Integration** - Connect ViewModels to new endpoints, error handling
- [ ] **T10: E2E Testing & Deployment** - Full flow testing, migration, rollout

## Dependencies

### Internal Dependencies
**Must Complete First**:
- Current profile screen UI structure (already exists ✅)
- Room model and repository (already exists ✅)
- RoomMember table and permissions (already exists ✅)
- Authentication/session management (already exists ✅)

**Parallel Work Possible**:
- Backend API development (T2-T5) can proceed while frontend prepares (T6-T8)
- Discovery API (T4) and Join API (T5) are independent, can be built in parallel

### External Dependencies
**None** - Feature is self-contained, no third-party services required

### Cross-Team Dependencies
**QA Team**:
- Test plan for privacy toggle edge cases (concurrent toggles, member preservation)
- Load testing plan for discovery feed (10,000+ rooms scenario)

**DevOps Team**:
- Migration runbook and rollback plan
- Database backup before production migration
- Monitoring alerts for discovery endpoint performance

## Success Criteria (Technical)

### Performance Benchmarks
- ✅ Discovery feed loads in <1s for 1000+ rooms
- ✅ Privacy toggle completes in <500ms
- ✅ Join room validates password in <200ms
- ✅ Pagination response time <300ms

### Quality Gates
- ✅ Zero data loss during privacy toggle (test with 1000 concurrent toggles)
- ✅ Zero member access loss (validate all existing members retain access)
- ✅ 100% test coverage for password generation
- ✅ All API endpoints return correct HTTP status codes
- ✅ Frontend handles all error states gracefully

### Acceptance Criteria
- ✅ Room owner can toggle privacy and see immediate effect
- ✅ Private rooms generate unique 4-digit passwords
- ✅ Public rooms appear in discovery feed within 5 seconds of toggle
- ✅ Users can join public rooms without password
- ✅ Users cannot join private rooms with wrong password
- ✅ Existing members unaffected by any privacy change
- ✅ Discovery feed shows accurate member counts
- ✅ Password copy-to-clipboard works on Android

### Security Validation
- ✅ SQL injection attempts fail on room code input
- ✅ Password generation uses crypto-secure random
- ✅ Rate limiting enforced (5 join attempts per minute per user)
- ✅ Room owner validation prevents unauthorized privacy changes

## Estimated Effort

### Overall Timeline
**Total: 13 days (2.6 weeks)**

- Backend Foundation: 3 days
- Discovery & Join APIs: 3 days
- Frontend UI: 4 days
- Testing & Polish: 2 days
- Deployment: 1 day

### Resource Requirements
- 1 Backend Engineer (Go): 6 days
- 1 Frontend Engineer (Kotlin/Compose): 7 days
- 1 QA Engineer: 2 days (parallel with dev)
- 0.5 DevOps Engineer: 1 day (migration + deployment)

### Critical Path Items
1. **Database Migration** (Day 1) - Blocks all backend work
2. **Privacy Toggle API** (Day 2-3) - Blocks frontend privacy UI
3. **Join Room API** (Day 4-5) - Blocks frontend join screen
4. **E2E Testing** (Day 12) - Blocks deployment

**Parallelization**: Discovery UI (T7) can start while Join API (T5) completes. Profile UI (T6) can start as soon as Privacy API (T3) is done.

### Risk Buffer
**Add 20% buffer** (2.5 days) for:
- Unexpected edge cases in member preservation
- Performance tuning for discovery feed
- UI/UX refinements based on user feedback
- Rollback scenarios during deployment

**Adjusted Timeline: 16 days (3.2 weeks)**

---

**Document Version**: 1.0
**Last Updated**: 2025-11-07
**Status**: Ready for Task Breakdown
**Next Step**: `/pm:epic-decompose room-privacy-management` to create GitHub issues
