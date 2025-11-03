---
name: room-and-map-improvements
description: Fix critical UX issues in map search, room validation, and room-based filtering
status: backlog
created: 2025-11-03T01:48:08Z
---

# PRD: Room and Map Improvements

## Executive Summary

This PRD addresses critical user experience issues in the Play Daily application's map and room functionality. Currently, users face confusion with map search behavior, lack of room validation during join operations, missing password input fields, and incorrect location data when creating memos from the map. Most critically, the application fails to properly filter memos and map markers by room context, showing all content regardless of which room the user has joined.

These fixes will significantly improve user experience by making the app behave predictably and ensuring data isolation between rooms.

## Problem Statement

### Current Pain Points

1. **Map Search UX Issues**
   - Search doesn't execute immediately when button is clicked
   - Search triggers incorrectly when text is cleared
   - Search results don't dismiss after location selection
   - Selected location doesn't update map view

2. **Room Join Validation Missing**
   - Users can "join" non-existent rooms without error
   - No validation check before join operation
   - No password input field for protected rooms
   - Confusing UX when attempting to join invalid rooms

3. **Incorrect Location Data**
   - Memos created from map use current GPS location instead of map pin location
   - Users expect memo to save the location they're viewing on map
   - Leads to incorrect memo positioning

4. **Room Context Not Enforced**
   - **CRITICAL**: All memos visible regardless of current room
   - Map shows markers for all rooms, not just current room
   - No data isolation between different rooms
   - Users see other rooms' content inappropriately

### Why This Matters Now

- Users are experiencing confusion and data leakage across rooms
- Map functionality is unintuitive and error-prone
- Room system fails to provide basic isolation expected from room-based architecture
- These issues directly impact core user workflows (search, create memo, join room)

## User Stories

### Primary Personas

**Persona 1: Active Memo Creator**
- Creates location-based memos frequently
- Uses map to find and save places
- Expects precise location control

**Persona 2: Room Participant**
- Joins different rooms for different contexts (work, personal, travel)
- Expects to see only relevant room content
- Needs password protection for private rooms

**Persona 3: Map Browser**
- Searches for locations on map
- Navigates to found locations
- Creates memos at searched locations

### Detailed User Journeys

#### Journey 1: Map Search and Selection
```
As a user searching for a location
1. I open the map screen
2. I type a search query (e.g., "Starbucks Gangnam")
3. I click the search button
4. I EXPECT: Search executes immediately with results displayed
5. I select a location from search results
6. I EXPECT: Map pans to selected location AND search results dismiss
7. CURRENT PROBLEM: Search doesn't trigger on button click, results persist after selection
```

#### Journey 2: Creating Memo from Map Location
```
As a user creating a memo from a map pin
1. I navigate to a location on the map (not my current location)
2. I click "Create Memo" button
3. I EXPECT: Memo saves with the map pin's coordinates
4. CURRENT PROBLEM: Memo saves with my current GPS location instead
```

#### Journey 3: Joining a Room
```
As a user joining a password-protected room
1. I enter a room code
2. I EXPECT: System validates room exists
3. I EXPECT: Password input field appears if room is protected
4. I enter password
5. I join room successfully
6. CURRENT PROBLEMS:
   - No validation (can "join" non-existent rooms)
   - No password input field
   - No feedback on invalid rooms
```

#### Journey 4: Viewing Room-Specific Content
```
As a user in a specific room
1. I join "Work Trip 2025" room
2. I view the memo list
3. I EXPECT: Only memos from "Work Trip 2025" room
4. I view the map
5. I EXPECT: Only markers from "Work Trip 2025" room
6. CURRENT PROBLEM: ALL memos and markers from ALL rooms are visible
```

## Requirements

### Functional Requirements

#### FR1: Immediate Map Search Execution
- **FR1.1**: Search executes when search button is clicked
- **FR1.2**: Search does NOT execute when text input is cleared
- **FR1.3**: Search results display below search bar
- **FR1.4**: Clear search query button clears text without triggering search

**Acceptance Criteria:**
- [ ] Clicking search button triggers search API call
- [ ] Clearing text field does not trigger search
- [ ] Search results appear within 500ms of button click
- [ ] Search can be triggered by button OR enter key

#### FR2: Search Result Selection and Dismissal
- **FR2.1**: Selecting a result pans map to selected location
- **FR2.2**: Selecting a result dismisses search results overlay
- **FR2.3**: Selected location becomes map center with appropriate zoom
- **FR2.4**: Search query remains in input field for reference

**Acceptance Criteria:**
- [ ] Map animates to selected location within 300ms
- [ ] Search results list disappears after selection
- [ ] Zoom level is appropriate for location type (building, area, city)
- [ ] Search text remains visible in input field

#### FR3: Map Pin Location for Memo Creation
- **FR3.1**: Memo creation captures map center/pin location, NOT current GPS location
- **FR3.2**: Location preview shows map pin coordinates before save
- **FR3.3**: User can manually adjust pin position before creating memo
- **FR3.4**: Saved memo displays at correct map pin location

**Acceptance Criteria:**
- [ ] Memo latitude/longitude matches map pin coordinates
- [ ] Location preview displays before memo save
- [ ] Pin can be dragged to adjust position
- [ ] Saved memo marker appears at exact pin location on map

#### FR4: Room Existence Validation
- **FR4.1**: System validates room exists before join attempt
- **FR4.2**: Clear error message displayed if room doesn't exist
- **FR4.3**: Join button disabled during validation
- **FR4.4**: API endpoint: `GET /v0.1/room/:room_code/validate`

**Acceptance Criteria:**
- [ ] Room validation API called before join
- [ ] Error message: "Room not found. Please check the room code."
- [ ] Join button shows loading state during validation
- [ ] Invalid room code prevents join attempt

#### FR5: Room Password Input
- **FR5.1**: Password input field displayed for password-protected rooms
- **FR5.2**: Password field appears after successful room validation
- **FR5.3**: Password required before join button is enabled
- **FR5.4**: Incorrect password shows error message

**Acceptance Criteria:**
- [ ] Password field appears for protected rooms only
- [ ] Password input is masked (password type)
- [ ] Join button disabled until password entered
- [ ] Error message: "Incorrect password. Please try again."

#### FR6: Room-Based Content Filtering (CRITICAL)
- **FR6.1**: Memo list shows only memos from current room
- **FR6.2**: Map markers display only for current room's memos
- **FR6.3**: Room context persists across app navigation
- **FR6.4**: Switching rooms updates visible content immediately

**Acceptance Criteria:**
- [ ] GET /v0.1/memo includes room_id filter parameter
- [ ] Memo list filtered by current room ID
- [ ] Map markers filtered by current room ID
- [ ] Room switch triggers memo list refresh
- [ ] No memos from other rooms visible in any view
- [ ] Room ID stored in app state/session

### Non-Functional Requirements

#### NFR1: Performance
- Search results display within 500ms
- Map pan animation completes within 300ms
- Room validation completes within 1 second
- Content filtering has no perceptible delay

#### NFR2: User Experience
- All error messages are clear and actionable
- Loading states visible for async operations
- No UI flickering during content filtering
- Smooth animations for map and list updates

#### NFR3: Data Integrity
- Room filtering enforced at API level (not just client-side)
- Location coordinates stored with 6 decimal precision
- Password validation uses secure comparison

#### NFR4: Compatibility
- Works on Android API 24+ (existing app requirement)
- Map search works with Kakao Map API
- Room validation works with existing Room schema

## Technical Implementation Notes

### Backend Changes Required

1. **Room Validation Endpoint**
```
GET /v0.1/room/:room_code/validate
Response:
{
  "exists": boolean,
  "requires_password": boolean,
  "room_name": string (if exists)
}
```

2. **Memo List Filtering**
```
GET /v0.1/memo?room_id={room_id}
- Add room_id query parameter
- Filter memos WHERE room_id = :room_id
- Return only matching memos
```

3. **Room Join with Password**
```
POST /v0.1/room/join
Body:
{
  "room_code": string,
  "password": string (optional)
}
```

### Frontend Changes Required

1. **Map Search Component**
   - Add onClick handler to search button
   - Remove onTextChange search trigger
   - Implement result selection handler with map pan
   - Add result dismissal logic

2. **Memo Creation from Map**
   - Capture map center coordinates instead of GPS
   - Pass coordinates to memo creation screen
   - Display location preview before save

3. **Room Join Flow**
   - Add room validation API call
   - Add password input field (conditional render)
   - Add error message display
   - Disable join during validation

4. **Room Context State**
   - Store current room ID in app state
   - Pass room ID to memo fetch APIs
   - Filter map markers by room ID
   - Refresh content on room switch

## Success Criteria

### Quantitative Metrics
- **Map Search**: 100% of searches execute on button click
- **Location Accuracy**: 100% of map-created memos save correct coordinates
- **Room Validation**: 0% successful joins to non-existent rooms
- **Content Isolation**: 0% cross-room content visibility

### Qualitative Metrics
- Users report improved search experience
- No confusion about which room's content is visible
- Reduced support tickets about "wrong location" memos
- Increased confidence in room privacy

### Validation Tests
1. **Test 1**: Search executes on button click, not on text clear
2. **Test 2**: Selected search result dismisses list and pans map
3. **Test 3**: Memo created from map pin has pin coordinates, not GPS
4. **Test 4**: Joining non-existent room shows error
5. **Test 5**: Password-protected room requires password input
6. **Test 6**: Room A memos not visible in Room B
7. **Test 7**: Room A markers not visible on Room B map

## Constraints & Assumptions

### Technical Constraints
- Must work with existing Kakao Map SDK
- Must maintain current Room database schema
- Cannot break existing memo creation from current location flow
- Backend changes must be backward compatible during deployment

### Timeline Constraints
- High priority fixes (room filtering, validation)
- Medium priority fixes (map search UX)
- Low priority polish (animations, transitions)

### Resource Constraints
- Frontend developer: 1 person
- Backend developer: 1 person
- Existing Room and Memo APIs must be extended, not replaced

### Assumptions
- Users understand concept of "rooms" as isolated workspaces
- Kakao Map API supports programmatic pan/zoom
- Room password is stored securely (hashed)
- Current GPS location is still needed for "create memo at current location" feature

## Out of Scope

### Explicitly NOT Building
- Room creation UI improvements
- Multi-room memo visibility (showing memos from multiple rooms)
- Room invitation system
- Map search history
- Saved favorite locations
- Offline map caching
- Custom map markers per room
- Room permission levels (admin, member, viewer)

### Future Considerations
- Advanced room filtering (show memos from selected rooms)
- Room discovery/browse feature
- Location sharing between room members
- Map annotation tools
- Place recommendations based on room context

## Dependencies

### External Dependencies
- **Kakao Map SDK**: Must support programmatic map control
- **Backend API**: Room validation and memo filtering endpoints
- **Android Location Services**: For GPS coordinates (when needed)

### Internal Dependencies
- **Room Service**: Must provide validation endpoint
- **Memo Service**: Must support room_id filtering
- **Auth Service**: Password validation for room join
- **State Management**: Room context must persist across screens

### Team Dependencies
- Backend team must implement validation and filtering endpoints first
- Frontend can begin UI changes in parallel
- QA must test room isolation thoroughly
- Design team to provide password input UI specs

## Implementation Phases

### Phase 1: Critical Fixes (Week 1)
- Room validation endpoint (Backend)
- Room-based content filtering (Backend + Frontend)
- Map pin location for memo creation (Frontend)

### Phase 2: Room Join UX (Week 2)
- Password input field (Frontend)
- Room validation UI (Frontend)
- Error messaging (Frontend)

### Phase 3: Map Search UX (Week 3)
- Immediate search execution (Frontend)
- Result selection and dismissal (Frontend)
- Map pan animation (Frontend)

### Phase 4: Testing & Polish (Week 4)
- Integration testing
- Room isolation validation
- Performance optimization
- Bug fixes

## Risk Assessment

### High Risk
- **Room filtering performance**: Large memo lists may cause lag
  - Mitigation: Add pagination, implement caching
- **Data migration**: Existing memos need room_id if not set
  - Mitigation: Default to user's default room

### Medium Risk
- **Map API rate limits**: Frequent searches may hit limits
  - Mitigation: Implement debouncing, local caching
- **Password security**: Secure password handling required
  - Mitigation: Use bcrypt, HTTPS only

### Low Risk
- **UI/UX confusion**: Users may not understand new validation flow
  - Mitigation: Add tooltips, onboarding hints
- **Backward compatibility**: Old app versions during rollout
  - Mitigation: API versioning, graceful degradation

## Appendix

### API Specifications

#### Room Validation
```typescript
GET /v0.1/room/:room_code/validate

Response 200:
{
  "exists": true,
  "requires_password": true,
  "room_name": "Work Trip 2025",
  "member_count": 5
}

Response 404:
{
  "exists": false,
  "message": "Room not found"
}
```

#### Memo Filtering
```typescript
GET /v0.1/memo?room_id=123

Response 200:
{
  "memos": [
    {
      "id": 1,
      "room_id": 123,
      "title": "Meeting Notes",
      "latitude": 37.5665,
      "longitude": 126.9780,
      ...
    }
  ],
  "total": 42
}
```

### Database Schema Changes

```sql
-- Add room_id index for performance
CREATE INDEX idx_memos_room_id ON memos(room_id);

-- Add password field to rooms if not exists
ALTER TABLE rooms ADD COLUMN password_hash VARCHAR(255);
ALTER TABLE rooms ADD COLUMN requires_password BOOLEAN DEFAULT FALSE;
```

### UI Mockup Notes

1. **Search Results Overlay**
   - Dropdown below search bar
   - Each result shows: name, address, distance
   - Dismiss on selection or outside click

2. **Password Input Modal**
   - Appears after valid room code entered
   - Shows room name
   - Masked password input
   - Cancel and Join buttons

3. **Room Context Indicator**
   - Current room name in app bar
   - Subtle indicator that content is filtered
   - Quick room switch button

---

**Document Version**: 1.0
**Last Updated**: 2025-11-03T01:48:08Z
**Owner**: Product Team
**Stakeholders**: Engineering, QA, Design
