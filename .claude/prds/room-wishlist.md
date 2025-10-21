---
name: room-wishlist
description: Room-scoped wishlist feature with interest ratings for places users want to visit
status: backlog
created: 2025-10-21T02:27:45Z
---

# PRD: Room-Based Wishlist Feature

## Executive Summary

The Daily Memo app currently allows users to save visited places with ratings (1-5). This PRD introduces a **wishlist feature** that enables users to save places they want to visit with an **interest level** (1-5) instead of ratings. Wishlists are **room-scoped**, meaning each user's room (personal memo space) will have its own set of wishlists and regular memos, ensuring data isolation and proper sharing within collaborative rooms.

**Value Proposition**: Users can plan future visits separately from their visit history, with interest-based prioritization and seamless room-based organization.

## Problem Statement

### Current State
- Users can only save places they've already visited (with ratings)
- No way to track places they want to visit in the future
- All memos are currently global - no room-based filtering exists
- Map markers and lists show all data regardless of which room the user is viewing

### Problems Being Solved
1. **No Future Planning**: Users cannot distinguish between "places I've been" vs "places I want to go"
2. **Data Clutter**: When switching between rooms (personal or shared), all memos from all rooms are visible
3. **Collaboration Confusion**: In shared rooms, users see memos from other rooms mixed with the current room's data
4. **Interest vs Rating Confusion**: Using ratings for unvisited places doesn't make semantic sense

### Why Now?
- Users are requesting the ability to plan visits
- Room collaboration feature already exists, but data isn't properly scoped
- Map functionality shows too much information at once

## User Stories

### Primary Personas

**Persona 1: Solo Planner (개인 사용자)**
- Age: 25-35
- Use case: Planning weekend trips, tracking restaurant recommendations
- Pain point: Can't keep track of places friends recommend without mixing them with visited places

**Persona 2: Group Traveler (그룹 여행자)**
- Age: 20-40
- Use case: Planning group trips with friends/family in shared rooms
- Pain point: Everyone's wishlists get mixed up, can't see who wants to visit what

**Persona 3: Foodie Explorer (맛집 탐방가)**
- Age: 25-45
- Use case: Collecting restaurant recommendations and rating interest level
- Pain point: Visited vs want-to-visit restaurants all look the same

### User Journeys

#### Journey 1: Creating a Wishlist Item
```
1. User opens app → Currently in "My Room" (개인방)
2. Navigates to "Wishlist" tab
3. Searches for a place or adds from map
4. Sets interest level 1-5 (5 = highest interest)
5. Optionally adds notes, saves business info from Naver/Kakao
6. Wishlist item created → appears in current room only
7. Map marker appears for this wishlist location (in current room view)
```

**Acceptance Criteria**:
- ✅ Wishlist tab is separate from regular memo list
- ✅ Interest level selector (1-5 stars/hearts)
- ✅ Can add place info from Naver/Kakao (location, phone, etc.)
- ✅ Wishlist item only visible in current room

#### Journey 2: Switching Rooms and Viewing Data
```
1. User is in "My Room" with 10 wishlists and 20 regular memos
2. User enters friend's user_id to join their room
3. App switches to friend's room
4. All lists (wishlist + regular) refresh to show ONLY friend's room data
5. Map markers update to show only friend's room locations
6. "Exit Room" button appears (since it's not user's own room)
7. User clicks "Exit" → returns to "My Room" → data switches back
```

**Acceptance Criteria**:
- ✅ Data completely refreshes when switching rooms
- ✅ No data from other rooms is visible
- ✅ Map markers filter by current room
- ✅ Exit button only shows when in someone else's room

#### Journey 3: Editing Interest Level
```
1. User views their wishlist
2. Clicks on a wishlist item
3. Updates interest level from 3 to 5
4. Optionally updates notes or business info
5. Saves → wishlist updated
```

**Acceptance Criteria**:
- ✅ Can modify interest level after creation
- ✅ Can update all metadata
- ✅ Changes persist and reflect immediately

#### Journey 4: Converting Wishlist to Regular Memo (After Visit)
```
Note: Not required in V1
Future consideration: After visiting a place on wishlist, user might want to
convert it to a regular memo with rating instead of interest level.
```

## Requirements

### Functional Requirements

#### FR1: Wishlist Data Model
- **FR1.1**: Create `is_wishlist` boolean field in memo table
- **FR1.2**: Existing `rating` field (1-5) becomes `interest_level` when `is_wishlist = true`
- **FR1.3**: Wishlist items belong to a specific room (user_id acts as room_id)
- **FR1.4**: Support business information fields:
  - Place name (from Kakao/Naver)
  - Address
  - Phone number
  - Kakao Map link
  - Naver Map link

#### FR2: Room-Based Data Filtering
- **FR2.1**: Backend API must filter memos by `user_id` (room_id)
- **FR2.2**: Frontend must pass current `room_id` in all list/map requests
- **FR2.3**: When switching rooms, clear local cache and fetch new room data
- **FR2.4**: Map markers must filter by current room

#### FR3: Wishlist UI
- **FR3.1**: Add "Wishlist" tab separate from regular memo list
- **FR3.2**: Wishlist creation screen with:
  - Interest level selector (1-5, 5=highest)
  - Place search (Kakao API integration)
  - Business info fields (optional)
  - Notes field
  - Location picker (map integration)
- **FR3.3**: Wishlist list view showing:
  - Place name
  - Interest level (stars/hearts)
  - Preview of business info
  - Location on map thumbnail
- **FR3.4**: Wishlist detail/edit screen

#### FR4: Room Switching
- **FR4.1**: User enters another user's ID to join their room
- **FR4.2**: Display "Exit Room" button when in someone else's room
- **FR4.3**: On room switch, reload all data (wishlists + regular memos + map markers)
- **FR4.4**: Clear indication of which room user is currently viewing

#### FR5: Map Integration
- **FR5.1**: Map markers show different icons for:
  - Regular memos (visited places)
  - Wishlists (want to visit)
- **FR5.2**: Map markers filter by current room
- **FR5.3**: Clicking wishlist marker opens wishlist detail

### Non-Functional Requirements

#### NFR1: Performance
- Room data switch must complete within 2 seconds
- Map marker refresh must be smooth (no flickering)
- List scrolling must remain 60fps even with 100+ items

#### NFR2: Data Integrity
- Room data must never leak between rooms
- Switching rooms must guarantee complete data refresh
- No stale data from previous room

#### NFR3: User Experience
- Clear visual distinction between wishlist and regular memo tabs
- Interest level must be intuitive (5 stars = most interested)
- Room switching must have loading indicator

#### NFR4: Scalability
- Support up to 500 wishlist items per room
- Support up to 1000 regular memos per room
- Efficient query performance with proper indexing on `user_id` + `is_wishlist`

## Success Criteria

### Measurable Outcomes
1. **Adoption Rate**: 60% of active users create at least 1 wishlist within first week
2. **Engagement**: Average 5 wishlist items per user per month
3. **Room Switching**: Average user switches rooms 3+ times per week
4. **Data Accuracy**: 0 incidents of data leaking between rooms
5. **Performance**: 95% of room switches complete within 2 seconds

### Key Metrics (KPIs)
- Wishlist creation rate
- Interest level distribution (how many 5s vs 1s)
- Wishlist-to-visit conversion (future: track when wishlist becomes regular memo)
- Room switching frequency
- Map usage increase (measuring if wishlist markers improve map engagement)

## Technical Specifications

### Database Schema Changes

```sql
-- Add wishlist flag to existing memo table
ALTER TABLE memos ADD COLUMN is_wishlist BOOLEAN DEFAULT false;

-- Semantic interpretation:
-- If is_wishlist = false: `rating` field = visit rating (1-5)
-- If is_wishlist = true: `rating` field = interest level (1-5)

-- Add business info fields (optional, can be null)
ALTER TABLE memos ADD COLUMN business_name VARCHAR(255);
ALTER TABLE memos ADD COLUMN business_phone VARCHAR(50);
ALTER TABLE memos ADD COLUMN business_address TEXT;
ALTER TABLE memos ADD COLUMN kakao_map_link VARCHAR(500);
ALTER TABLE memos ADD COLUMN naver_map_link VARCHAR(500);

-- Add index for efficient room-based queries
CREATE INDEX idx_memos_room_wishlist ON memos(user_id, is_wishlist);
```

### API Changes

#### New/Modified Endpoints

**GET /v0.1/memo**
- Add query parameter: `room_id` (required)
- Add query parameter: `is_wishlist` (optional, boolean)
- Filter results by `user_id = room_id`

**POST /v0.1/memo**
- Add field: `is_wishlist` (boolean, default: false)
- Add fields: business info (optional)

**PUT /v0.1/memo/:id**
- Allow updating `interest_level` (when is_wishlist=true)
- Allow updating business info

**Example Request (Create Wishlist)**:
```json
{
  "title": "서울 핫플 카페",
  "is_wishlist": true,
  "rating": 5,  // interest_level
  "latitude": 37.5665,
  "longitude": 126.9780,
  "location_name": "강남역",
  "business_name": "블루보틀 강남점",
  "business_phone": "02-1234-5678",
  "business_address": "서울시 강남구 ...",
  "kakao_map_link": "https://map.kakao.com/...",
  "content": "친구 추천 받은 카페"
}
```

### Frontend Changes

#### New Components
- `WishlistTab.kt` - Wishlist tab UI
- `WishlistCreateScreen.kt` - Create wishlist screen
- `WishlistDetailScreen.kt` - View/edit wishlist
- `RoomSwitcher.kt` - Room selection UI
- `InterestLevelPicker.kt` - 1-5 star/heart selector

#### State Management
```kotlin
data class RoomState(
  val currentRoomId: Long,  // Currently viewing room (user_id)
  val isOwnRoom: Boolean,    // True if currentRoomId == myUserId
  val wishlists: List<Memo>, // Filtered by room
  val regularMemos: List<Memo> // Filtered by room
)

// When switching rooms:
fun switchRoom(newRoomId: Long) {
  viewModelScope.launch {
    // Clear current data
    _wishlists.value = emptyList()
    _regularMemos.value = emptyList()

    // Fetch new room data
    val newWishlists = repository.getMemos(roomId = newRoomId, isWishlist = true)
    val newMemos = repository.getMemos(roomId = newRoomId, isWishlist = false)

    // Update state
    _currentRoomId.value = newRoomId
    _wishlists.value = newWishlists
    _regularMemos.value = newMemos
  }
}
```

## Constraints & Assumptions

### Technical Constraints
- `user_id` serves as `room_id` (no separate room table in V1)
- Must maintain backward compatibility with existing memos
- Kakao/Naver API rate limits apply to business info fetching

### Business Constraints
- V1 focuses on core wishlist functionality only
- No wishlist sharing between rooms in V1
- No wishlist-to-memo conversion in V1 (manual re-entry required)

### Assumptions
- Users understand the concept of "room" as personal memo space
- Interest level 1-5 is intuitive (5 = most interested)
- Business info from Kakao/Naver is sufficient (no custom fields needed)

## Out of Scope (V1)

### Explicitly NOT Building
1. **Wishlist Sharing**: No exporting/importing wishlists between rooms
2. **Wishlist Conversion**: No automatic conversion from wishlist → regular memo after visit
3. **Collaborative Wishlist Editing**: No multi-user editing of same wishlist item
4. **Advanced Filtering**: No filtering by interest level, business type, etc.
5. **Wishlist Categories**: No custom categories or tags for wishlists
6. **Visit Reminders**: No notifications or reminders for wishlists
7. **KakaoTalk Sharing**: Mentioned as future possibility, not V1

### Future Considerations
- V2: Wishlist sharing via KakaoTalk (send business link)
- V2: One-tap conversion from wishlist to memo
- V2: Visit planning calendar integration
- V3: Collaborative wishlist editing in shared rooms

## Dependencies

### External Dependencies
- **Kakao Maps API**: Place search, business info, map links
- **Naver Maps API**: Alternative business info source
- **Backend API**: Must implement room-based filtering

### Internal Team Dependencies
- **Backend Team**: Database schema changes, API endpoint updates
- **Android Team**: UI implementation, state management
- **Design Team**: Wishlist tab design, interest level UI/UX

### Timeline Dependencies
- Schema migration must complete before API development
- API development must complete before frontend integration
- Room-based filtering is prerequisite for wishlist feature

## Implementation Phases

### Phase 1: Backend Foundation (Week 1-2)
- Database schema changes
- Add `is_wishlist` field
- Add business info fields
- Create indexes
- Update API to support room_id filtering

### Phase 2: API Development (Week 2-3)
- Modify GET /memo to support room_id + is_wishlist
- Modify POST /memo for wishlist creation
- Modify PUT /memo for interest level updates
- Add business info field support

### Phase 3: Frontend Implementation (Week 3-5)
- Create wishlist tab UI
- Implement interest level picker
- Build wishlist create/edit screens
- Add room switching logic
- Update map markers with wishlist support

### Phase 4: Testing & Polish (Week 5-6)
- Room data isolation testing
- Performance testing (room switching speed)
- UI/UX polish
- Bug fixes

## Open Questions

1. **Business Info Fetching**: Should we fetch business info automatically when user selects a place, or make it optional?
   - **Recommendation**: Auto-fetch but allow manual override

2. **Interest Level Decay**: Should old wishlists automatically decrease interest level over time?
   - **Recommendation**: Not in V1, consider for V2

3. **Duplicate Detection**: What if user adds same place to wishlist multiple times?
   - **Recommendation**: Show warning but allow (user might have different contexts)

4. **Default Interest Level**: What should default interest level be when creating wishlist?
   - **Recommendation**: Default to 3 (neutral), force user to think about it

## Risk Assessment

### High Risk
- **Data Leakage**: Memos from wrong room showing up
  - **Mitigation**: Comprehensive testing, backend validation

### Medium Risk
- **Performance**: Slow room switching with large datasets
  - **Mitigation**: Proper indexing, pagination, caching strategy

### Low Risk
- **User Confusion**: Users don't understand interest level vs rating
  - **Mitigation**: Clear UI labels, onboarding tutorial

## Appendix

### Related Features
- Existing memo system
- Map marker system
- Room collaboration system

### References
- Kakao Maps API documentation
- Naver Place API documentation
- Current memo data model

### Glossary
- **Room**: Personal memo space (user_id), can be shared with others
- **Wishlist**: Place user wants to visit (is_wishlist = true)
- **Interest Level**: 1-5 rating for unvisited places (5 = most interested)
- **Regular Memo**: Place user has visited (is_wishlist = false)
- **Rating**: 1-5 rating for visited places
