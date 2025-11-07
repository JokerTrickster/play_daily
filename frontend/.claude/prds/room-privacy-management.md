---
name: room-privacy-management
description: Room visibility control system with public/private modes, password management, and discovery features
status: backlog
created: 2025-11-07T07:52:34Z
---

# PRD: Room Privacy Management

## Executive Summary

This feature enables room owners to control their room's visibility and access through a public/private toggle system. Public rooms are discoverable by all users without password requirements, while private rooms require a 4-digit password for access. The system includes automated password generation for private rooms, a discovery interface for browsing public rooms by popularity, and direct room access via room ID input.

**Key Value Propositions:**
- Room owners gain granular control over room privacy
- Public rooms increase discoverability and community engagement
- Private rooms maintain security with automatic password regeneration
- Existing room members remain unaffected by privacy changes

## Problem Statement

**Current Pain Points:**
- All rooms currently use the same privacy model (user's room password)
- No distinction between public community rooms and private personal spaces
- No discovery mechanism for users to find interesting rooms
- Room owners cannot control who can discover their rooms

**Why This Matters Now:**
- Users need flexibility between public sharing and private collaboration
- Community growth depends on room discoverability
- Privacy-conscious users require better access control
- Current system creates friction for both public and private use cases

## User Stories

### Primary Personas

**1. Room Owner (Creator)**
- **Profile**: User who created a room and manages its settings
- **Goals**: Control room visibility, manage access, maintain existing collaborations
- **Pain Points**: Cannot choose between public discovery vs. private access

**2. Room Seeker (Browser)**
- **Profile**: User looking for interesting rooms to join
- **Goals**: Discover popular public rooms, join communities of interest
- **Pain Points**: No way to find rooms beyond direct invitation

**3. Room Member (Collaborator)**
- **Profile**: User who has joined a room and actively participates
- **Goals**: Continue accessing room regardless of privacy changes
- **Pain Points**: Concerns about losing access during settings changes

### Detailed User Journeys

#### Journey 1: Converting Private Room to Public

**Actor**: Room Owner
**Precondition**: User owns a private room with password

**Steps:**
1. Navigate to profile screen → "My Room Settings"
2. See current status: "Private Room" with password displayed
3. Tap "Make Public" button
4. See confirmation dialog: "Making room public will remove password requirement. Current members will remain. Continue?"
5. Confirm action
6. Password field becomes empty/null
7. Room status changes to "Public"
8. Room becomes visible in public discovery feed

**Acceptance Criteria:**
- Password is removed from database (set to NULL or empty string)
- Existing room members (RoomMember table) remain unchanged
- Room appears in public room list immediately
- Room owner receives confirmation notification

#### Journey 2: Converting Public Room to Private

**Actor**: Room Owner
**Precondition**: User owns a public room without password

**Steps:**
1. Navigate to profile screen → "My Room Settings"
2. See current status: "Public Room"
3. Tap "Make Private" button
4. System automatically generates 4-digit password (e.g., "5132")
5. See confirmation dialog showing new password: "Your room is now private. Password: 5132. Current members can still access without password."
6. Confirm and copy password
7. Room status changes to "Private"
8. Room is removed from public discovery feed

**Acceptance Criteria:**
- Server generates random 4-digit numeric password (0000-9999)
- Password is stored in Room table
- Existing room members retain access without password requirement
- New users must enter password to join
- Room removed from public listing

#### Journey 3: Discovering Public Rooms

**Actor**: Room Seeker
**Precondition**: None

**Steps:**
1. Navigate to "Discover Rooms" screen
2. See list of public rooms sorted by likes (descending)
3. Each room card shows:
   - Room name
   - Owner nickname
   - Likes count
   - Member count
   - "Public" badge
4. Scroll through paginated list (10 rooms per page)
5. Tap "Join" button on desired room
6. Immediately added as READ_ONLY member
7. Redirect to room view

**Acceptance Criteria:**
- Only public rooms displayed (is_public = true)
- Sorted by likes_count descending
- Pagination at 10 items per page
- Join action requires no password
- User added with READ_ONLY permission

#### Journey 4: Direct Room Access via ID

**Actor**: Room Seeker
**Precondition**: User has room ID from external source

**Steps:**
1. Navigate to "Join Room" screen
2. See input field: "Enter Room ID"
3. Enter room ID (e.g., "abc123xyz")
4. Tap "Join" button
5. **If Public Room:**
   - Immediately added as READ_ONLY member
   - Redirect to room view
6. **If Private Room:**
   - Prompted for password input
   - Enter 4-digit password
   - If correct: added as READ_ONLY member
   - If incorrect: show error "Invalid password"

**Acceptance Criteria:**
- Room ID lookup succeeds for existing rooms
- Public rooms bypass password requirement
- Private rooms require correct password
- Invalid room IDs show clear error message
- Successful join redirects to room content

## Requirements

### Functional Requirements

#### FR-1: Room Privacy Toggle
**Priority**: P0 (Must Have)

- Room owner can toggle between Public/Private modes
- Toggle available in Profile → My Room Settings
- Current state clearly displayed with visual indicator
- Toggle action triggers confirmation dialog
- State persists immediately upon confirmation

#### FR-2: Automatic Password Management
**Priority**: P0 (Must Have)

**Private Mode:**
- System generates random 4-digit password (0000-9999)
- Password generated server-side for security
- Password displayed to owner after generation
- Password stored in Room table `password` field

**Public Mode:**
- Password field set to NULL or empty string
- No password required for joining
- Previous password cannot be recovered

#### FR-3: Member Preservation
**Priority**: P0 (Must Have)

- Existing RoomMember records unchanged during privacy toggle
- Members retain original permission levels (READ_ONLY, READ_WRITE, OWNER)
- Member access unaffected by privacy changes
- Only new join requests subject to new privacy rules

#### FR-4: Public Room Discovery Feed
**Priority**: P0 (Must Have)

**UI Components:**
- "Discover Rooms" screen in navigation
- Grid/List view of public rooms
- Room cards displaying:
  - Room name
  - Owner nickname
  - Likes count
  - Member count
  - "Public" badge

**Behavior:**
- Fetches rooms where `is_public = true`
- Sorts by `likes_count DESC`
- Paginated response (10 items per page)
- Infinite scroll or "Load More" button
- Pull-to-refresh support

**API Endpoint:**
```
GET /v0.1/rooms/public?page=1&limit=10
Response:
{
  "rooms": [
    {
      "id": 123,
      "room_code": "abc123",
      "name": "Travel Lovers Korea",
      "owner": {
        "user_id": 45,
        "nickname": "JohnDoe"
      },
      "likes_count": 89,
      "members_count": 12,
      "is_public": true
    }
  ],
  "pagination": {
    "current_page": 1,
    "total_pages": 5,
    "total_count": 47
  }
}
```

#### FR-5: Direct Room Access by ID
**Priority**: P0 (Must Have)

**UI Components:**
- "Join Room" screen with input field
- Room ID input (alphanumeric)
- "Join" button
- Password modal (conditional on private rooms)

**Behavior:**
- Validate room ID format
- Check if room exists
- Check room privacy status
- **If Public**: direct join
- **If Private**: prompt for password
- Validate password if required
- Add user to room_members with READ_ONLY permission
- Redirect to room view

**API Endpoint:**
```
POST /v0.1/rooms/join
Request:
{
  "room_code": "abc123",
  "password": "5132"  // optional, only for private rooms
}

Response (Success):
{
  "room_id": 123,
  "permission": "READ_ONLY",
  "message": "Successfully joined room"
}

Response (Error - Wrong Password):
{
  "error": "INVALID_PASSWORD",
  "message": "Incorrect room password"
}
```

#### FR-6: Room Likes for Members Only
**Priority**: P1 (Should Have)

- Only room members can like a room
- Likes stored in `room_likes` table
- `likes_count` on Room incremented/decremented
- Like action requires membership validation

### Non-Functional Requirements

#### NFR-1: Performance
- Room discovery feed loads within 1 second
- Password generation completes within 100ms
- Privacy toggle updates database within 500ms
- Pagination responses under 500ms

#### NFR-2: Security
- Passwords generated with cryptographically secure random
- Room IDs validated against SQL injection
- Rate limiting on join attempts (5 per minute per user)
- Password transmitted over HTTPS only

#### NFR-3: Data Integrity
- Privacy toggle is atomic transaction
- Member preservation guaranteed during toggle
- Concurrent privacy changes handled with optimistic locking
- Password changes logged for audit trail

#### NFR-4: Scalability
- Public room discovery supports 10,000+ rooms
- Efficient indexing on `is_public` and `likes_count`
- Database query optimization for sorted pagination
- Caching layer for popular rooms list (5-minute TTL)

#### NFR-5: Usability
- Privacy status visible at all times
- Confirmation dialogs prevent accidental changes
- Password copying enabled with single tap
- Clear error messages for all failure cases

## Database Schema Changes

### Migration: `migration_add_room_privacy.sql`

```sql
-- Add privacy and password fields to rooms table
ALTER TABLE rooms
ADD COLUMN is_public BOOLEAN NOT NULL DEFAULT false
    COMMENT 'Room visibility (true=public, false=private)' AFTER owner_user_id,
ADD COLUMN password VARCHAR(4) NULL
    COMMENT '4-digit room password (NULL for public rooms)' AFTER is_public,
ADD INDEX idx_room_discovery (is_public, likes_count, deleted_at);

-- Update existing rooms to match user's room password (migration compatibility)
UPDATE rooms r
INNER JOIN users u ON u.default_room_id = r.id
SET r.password = u.room_password,
    r.is_public = false
WHERE r.password IS NULL;

-- Add index for member validation during like operations
CREATE INDEX idx_room_members_validation ON room_members(room_id, user_id, deleted_at);
```

### Updated Room Model (Go)

```go
type Room struct {
    gorm.Model
    RoomCode    string  `json:"room_code" gorm:"column:room_code;type:varchar(50);uniqueIndex;not null"`
    Name        string  `json:"name" gorm:"column:name;type:varchar(100);not null"`
    LikesCount  uint    `json:"likes_count" gorm:"column:likes_count;type:int unsigned;default:0;index"`
    OwnerUserID uint    `json:"owner_user_id" gorm:"column:owner_user_id;not null;index"`
    IsPublic    bool    `json:"is_public" gorm:"column:is_public;not null;default:false;index"` // NEW
    Password    *string `json:"password,omitempty" gorm:"column:password;type:varchar(4)"` // NEW
    Owner       *User   `json:"owner,omitempty" gorm:"foreignKey:OwnerUserID"`
    Memos       []Memo  `json:"memos,omitempty" gorm:"foreignKey:RoomID"`
}
```

## Success Criteria

### Quantitative Metrics

**Primary KPIs:**
- **Public Room Adoption**: 30% of active rooms set to public within 1 month
- **Discovery Engagement**: 20% of weekly active users visit discovery feed
- **Join Success Rate**: >90% of join attempts successful (valid password or public)
- **Member Retention**: 0% membership loss during privacy toggles

**Secondary KPIs:**
- Average likes per public room increases by 50%
- Discovery-driven joins account for 15% of new room memberships
- Password generation errors < 0.1%

### Qualitative Outcomes

**User Satisfaction:**
- Room owners report increased control over privacy
- New users successfully discover and join public rooms
- Private room users feel secure with password protection
- No user reports of lost room access due to privacy changes

**Technical Quality:**
- Zero data integrity issues during privacy toggles
- Zero member data loss
- Privacy toggle completes in single API call
- Rollback capability for failed migrations

## Constraints & Assumptions

### Technical Constraints

- Database supports VARCHAR(4) for password field
- Room codes (IDs) are already unique and indexed
- Existing `room_members` table has proper indexes
- Backend can generate secure random numbers

### Resource Constraints

- Development: 2 weeks (1 backend, 1 frontend)
- Testing: 1 week for QA and edge cases
- Migration: 30 minutes downtime for schema update
- Database storage: +8 bytes per room (negligible)

### Business Assumptions

- Users understand public/private distinction
- Room owners will opt-in to public mode voluntarily
- 4-digit passwords provide sufficient security for casual use
- Discovery feed will drive user engagement

### Technical Assumptions

- Room IDs remain immutable
- Password does not need encryption (stored plaintext for 4-digit numeric)
- Existing room members authenticated via session token
- Frontend can handle conditional password input flows

## Out of Scope

**Explicitly NOT Included in V1:**

1. **Password Customization**: Users cannot choose custom passwords (auto-generated only)
2. **Room Invitations**: No invite system; users must know room ID or find in discovery
3. **Room Categories**: No categorization or filtering of public rooms by topic
4. **Advanced Search**: No search by room name or owner in discovery feed
5. **Password Reset**: No "forgot password" flow; owner must toggle privacy to regenerate
6. **Member Notifications**: Members not notified when room privacy changes
7. **Access Logs**: No audit trail of who joined via password vs. discovery
8. **Tiered Permissions**: No ability to require password for certain permission levels only
9. **Room Description**: No description field in discovery cards
10. **Like Restrictions**: No cooldown or like removal functionality

**Future Considerations:**
- Room categories/tags for discovery filtering
- Search functionality in discovery feed
- Member notifications on privacy changes
- Custom password option for premium users
- Advanced analytics on room discovery metrics

## Dependencies

### Internal Dependencies

**Backend:**
- Migration script execution (requires DB access)
- Password generation utility function
- Room query optimization for discovery feed
- Member validation service for like operations

**Frontend:**
- Profile screen UI refactor for privacy toggle
- New "Discover Rooms" screen implementation
- Password input modal component
- Room card component for discovery feed

**Database:**
- Schema migration successful
- Indexes created for performance
- Data backfill for existing rooms

### External Dependencies

**None** - Feature is self-contained within existing infrastructure

### Cross-Team Dependencies

**QA Team:**
- Test plan for privacy toggle edge cases
- Load testing for discovery feed pagination
- Security audit for password generation

**DevOps:**
- Migration runbook for production deployment
- Monitoring alerts for discovery feed performance
- Database backup before migration

## API Specifications

### 1. Toggle Room Privacy

**Endpoint:** `PUT /v0.1/rooms/{room_id}/privacy`

**Request:**
```json
{
  "is_public": true
}
```

**Response (Public):**
```json
{
  "room_id": 123,
  "is_public": true,
  "password": null,
  "message": "Room is now public"
}
```

**Response (Private):**
```json
{
  "room_id": 123,
  "is_public": false,
  "password": "5132",
  "message": "Room is now private. Password: 5132"
}
```

**Error Cases:**
- `403 FORBIDDEN`: User is not room owner
- `404 NOT_FOUND`: Room does not exist
- `500 INTERNAL_ERROR`: Password generation failed

### 2. Get Public Rooms

**Endpoint:** `GET /v0.1/rooms/public`

**Query Parameters:**
- `page` (int, default: 1): Page number
- `limit` (int, default: 10, max: 50): Items per page

**Response:**
```json
{
  "rooms": [
    {
      "id": 123,
      "room_code": "abc123xyz",
      "name": "Seoul Food Lovers",
      "owner": {
        "user_id": 45,
        "nickname": "FoodieKim",
        "profile_image_url": "https://..."
      },
      "likes_count": 89,
      "members_count": 23,
      "is_public": true,
      "created_at": "2025-01-15T10:30:00Z"
    }
  ],
  "pagination": {
    "current_page": 1,
    "total_pages": 8,
    "total_count": 76,
    "has_next": true
  }
}
```

### 3. Join Room by ID

**Endpoint:** `POST /v0.1/rooms/join`

**Request:**
```json
{
  "room_code": "abc123xyz",
  "password": "5132"  // optional, required only for private rooms
}
```

**Response (Success):**
```json
{
  "room_id": 123,
  "room_name": "Seoul Food Lovers",
  "permission": "READ_ONLY",
  "is_public": false,
  "message": "Successfully joined room"
}
```

**Error Cases:**
- `404 NOT_FOUND`: Room code does not exist
- `401 INVALID_PASSWORD`: Password incorrect for private room
- `400 PASSWORD_REQUIRED`: Private room requires password
- `409 ALREADY_MEMBER`: User already a member of this room

### 4. Like/Unlike Room (Member Only)

**Endpoint:** `POST /v0.1/rooms/{room_id}/like`

**Request:** (Empty body)

**Response:**
```json
{
  "room_id": 123,
  "liked": true,
  "likes_count": 90
}
```

**Error Cases:**
- `403 NOT_MEMBER`: User must be room member to like
- `404 NOT_FOUND`: Room does not exist

## UI/UX Specifications

### Profile Screen - Privacy Toggle

**Location:** Profile Tab → My Room Settings

**Layout:**
```
┌─────────────────────────────┐
│  My Room Settings           │
├─────────────────────────────┤
│  Room Name: My Travel Log   │
│  Room ID: xyz789            │
│                             │
│  ┌───────────────────────┐  │
│  │ Room Visibility       │  │
│  │                       │  │
│  │ ○ Public   ● Private  │  │
│  │                       │  │
│  │ Password: 5132  [📋]  │  │  // Only shown if private
│  └───────────────────────┘  │
│                             │
│  [Update Privacy]           │
└─────────────────────────────┘
```

**Confirmation Dialog (Public → Private):**
```
┌─────────────────────────────┐
│  Make Room Private?         │
├─────────────────────────────┤
│  A new password will be     │
│  generated. Current members │
│  will retain access.        │
│                             │
│  [Cancel]  [Confirm]        │
└─────────────────────────────┘
```

**Success Dialog (Private):**
```
┌─────────────────────────────┐
│  Room is Now Private        │
├─────────────────────────────┤
│  New Password: 5132         │
│                             │
│  [Copy Password]            │
│  [Done]                     │
└─────────────────────────────┘
```

### Discover Rooms Screen

**Layout:**
```
┌─────────────────────────────┐
│  < Discover Public Rooms    │
├─────────────────────────────┤
│  ┌─────────────────────────┐│
│  │ 🏠 Seoul Food Lovers    ││
│  │ by FoodieKim            ││
│  │ ❤️ 89 likes  👥 23     ││
│  │              [Join] →   ││
│  └─────────────────────────┘│
│  ┌─────────────────────────┐│
│  │ 🌄 Mountain Hikers      ││
│  │ by TrailBlazer          ││
│  │ ❤️ 67 likes  👥 15     ││
│  │              [Join] →   ││
│  └─────────────────────────┘│
│                             │
│  [Load More]                │
└─────────────────────────────┘
```

### Join Room by ID Screen

**Layout:**
```
┌─────────────────────────────┐
│  < Join Room                │
├─────────────────────────────┤
│  Enter Room ID              │
│  ┌─────────────────────────┐│
│  │ abc123xyz               ││
│  └─────────────────────────┘│
│                             │
│  [Join Room]                │
│                             │
│  Or browse public rooms:    │
│  [Discover Rooms]           │
└─────────────────────────────┘
```

**Password Modal (Private Room):**
```
┌─────────────────────────────┐
│  Private Room               │
├─────────────────────────────┤
│  Enter 4-digit password:    │
│                             │
│  ┌───┬───┬───┬───┐          │
│  │ 5 │ 1 │ 3 │ 2 │          │
│  └───┴───┴───┴───┘          │
│                             │
│  [Cancel]  [Join]           │
└─────────────────────────────┘
```

## Implementation Phases

### Phase 1: Backend Foundation (Week 1)

**Database:**
- Create migration script
- Execute migration on dev environment
- Backfill existing rooms with passwords

**API Development:**
- Implement privacy toggle endpoint
- Implement password generation service
- Add validation for room ownership

**Testing:**
- Unit tests for password generation
- Integration tests for privacy toggle
- Member preservation tests

### Phase 2: Discovery & Join (Week 1-2)

**Backend:**
- Implement public rooms discovery endpoint
- Implement join room endpoint with password validation
- Add member-only like validation

**Frontend:**
- Create Discover Rooms screen
- Implement room cards UI
- Add pagination/infinite scroll

**Testing:**
- Load testing for discovery feed
- Password validation edge cases
- Join flow end-to-end tests

### Phase 3: Frontend Integration (Week 2)

**UI Development:**
- Add privacy toggle to profile screen
- Create password display/copy UI
- Implement confirmation dialogs
- Build join room by ID screen

**Integration:**
- Connect all API endpoints
- Handle error states
- Add loading indicators

**Testing:**
- E2E tests for complete flows
- UX testing with real users
- Accessibility testing

### Phase 4: QA & Launch (Week 3)

**Quality Assurance:**
- Security audit for password handling
- Performance testing under load
- Cross-platform testing (iOS/Android)

**Deployment:**
- Production database migration
- Gradual rollout (10% → 50% → 100%)
- Monitor error rates and performance

**Documentation:**
- User guide for privacy settings
- API documentation updates
- Internal runbook for support team

## Risk Assessment

### High Priority Risks

**Risk 1: Member Loss During Privacy Toggle**
- **Impact**: Critical - Users lose room access
- **Probability**: Low
- **Mitigation**: Thorough testing, transaction rollback, member preservation validation

**Risk 2: Password Generation Collisions**
- **Impact**: Medium - Multiple rooms with same password
- **Probability**: Very Low (1 in 10,000)
- **Mitigation**: Use crypto-secure random, accept collision risk for 4-digit codes

**Risk 3: Discovery Feed Performance**
- **Impact**: Medium - Slow load times hurt UX
- **Probability**: Medium
- **Mitigation**: Database indexing, caching layer, pagination

### Medium Priority Risks

**Risk 4: Privacy Toggle Race Conditions**
- **Impact**: Low - Inconsistent state if concurrent toggles
- **Probability**: Very Low
- **Mitigation**: Optimistic locking, atomic transactions

**Risk 5: Spam Joins via Discovery**
- **Impact**: Low - Spam accounts join many rooms
- **Probability**: Medium
- **Mitigation**: Rate limiting, monitoring, future moderation tools

## Open Questions

1. **Password Security**: Should 4-digit passwords be hashed or stored plaintext?
   - **Recommendation**: Store plaintext for usability (easily shareable, not high-security context)

2. **Like Permissions**: Should non-members see like count on public rooms?
   - **Recommendation**: Yes, for social proof in discovery feed

3. **Privacy Default**: Should new rooms default to public or private?
   - **Recommendation**: Private (safer default, users opt-in to public)

4. **Migration Downtime**: Can migration run online or requires maintenance window?
   - **Recommendation**: Online migration possible, but schedule during low-traffic window

5. **Password Display**: Should password be shown in plaintext or masked with reveal option?
   - **Recommendation**: Plaintext with copy button (easier for sharing)

## Appendix

### Example Database Queries

**Get Public Rooms (Paginated):**
```sql
SELECT
  r.id,
  r.room_code,
  r.name,
  r.likes_count,
  u.id as owner_id,
  u.nickname as owner_nickname,
  COUNT(DISTINCT rm.id) as members_count
FROM rooms r
INNER JOIN users u ON u.id = r.owner_user_id
LEFT JOIN room_members rm ON rm.room_id = r.id AND rm.deleted_at IS NULL
WHERE r.is_public = true
  AND r.deleted_at IS NULL
GROUP BY r.id
ORDER BY r.likes_count DESC
LIMIT 10 OFFSET 0;
```

**Toggle Room Privacy (Private):**
```sql
-- Generate random 4-digit password in backend (e.g., Go: fmt.Sprintf("%04d", rand.Intn(10000)))
UPDATE rooms
SET is_public = false,
    password = '5132',
    updated_at = NOW()
WHERE id = 123
  AND owner_user_id = 45
  AND deleted_at IS NULL;
```

**Toggle Room Privacy (Public):**
```sql
UPDATE rooms
SET is_public = true,
    password = NULL,
    updated_at = NOW()
WHERE id = 123
  AND owner_user_id = 45
  AND deleted_at IS NULL;
```

**Validate Room Member for Like:**
```sql
SELECT EXISTS(
  SELECT 1
  FROM room_members
  WHERE room_id = 123
    AND user_id = 45
    AND deleted_at IS NULL
) as is_member;
```

### Glossary

- **Room Code**: Unique identifier for each room (e.g., "abc123xyz"), used for direct access
- **Room Owner**: User who created the room (owner_user_id), has full control
- **Room Member**: User with access to room (stored in room_members table)
- **Public Room**: Room with is_public=true, visible in discovery, no password required
- **Private Room**: Room with is_public=false, requires password, not in discovery
- **Permission Level**: Access level (READ_ONLY, READ_WRITE, OWNER) for room members
- **Discovery Feed**: UI screen showing public rooms sorted by popularity

---

**Document Version**: 1.0
**Last Updated**: 2025-11-07
**Status**: Ready for Review
**Next Step**: `/pm:prd-parse room-privacy-management` to create implementation epic
