---
name: room-search-discovery
description: Room search and discovery system with popular rooms, user search, and password reset functionality
status: backlog
created: 2025-11-11T03:29:05Z
---

# PRD: Room Search and Discovery

## Executive Summary

This PRD outlines a comprehensive room search and discovery system that enables users to find and join rooms through multiple pathways: browsing popular rooms, searching by user ID, and discovering newly created rooms. The system includes pagination support, user profile previews, and room password management.

**Key Value Propositions:**
- Discover trending rooms based on community engagement
- Find specific users' rooms through ID search
- Browse newly created rooms for fresh content
- Enhanced user profiles with bio/self-introduction
- Secure room access with password reset capabilities

## Problem Statement

### Current Pain Points
1. **Limited Room Discovery**: Users have no way to discover popular or interesting rooms beyond direct invitations
2. **No User Search**: Cannot find specific users' rooms by searching their user ID
3. **Poor Visibility**: New rooms remain hidden without discovery mechanisms
4. **Lack of User Context**: Cannot preview user profiles/bios before joining their rooms
5. **Password Management Gap**: No way to reset room passwords if forgotten or compromised

### Why This Matters Now
- Users report difficulty finding active, engaging rooms
- Community growth is limited by poor discovery mechanisms
- User retention suffers when new users can't find relevant content
- Security concerns with no password reset mechanism

## User Stories

### Primary Personas

**1. Sarah - New User**
- Goals: Find active rooms to join, discover interesting content
- Pain: Overwhelmed, doesn't know where to start
- Needs: Curated list of popular rooms, easy browsing

**2. Mike - Power User**
- Goals: Find specific users' rooms, discover new communities
- Pain: Manual searching is tedious
- Needs: User ID search, efficient filtering

**3. Emma - Room Owner**
- Goals: Attract new members, manage room access
- Pain: Lost password locks out management functions
- Needs: Password reset, visibility in discovery

### Detailed User Journeys

#### Journey 1: Discovering Popular Rooms
```
1. Sarah opens "방 검색" (Room Search) tab
2. Sees table of popular rooms sorted by likes
3. Each row shows:
   - Room name
   - Owner profile image (small)
   - Owner name
   - Like count
   - Member count
4. Clicks on owner profile image
5. Modal shows owner's bio and larger profile photo
6. Clicks "참여하기" to join the room
7. System validates and adds her to the room
```

#### Journey 2: Finding User by ID
```
1. Mike has a friend's user ID: "user123"
2. Enters ID in search box at top
3. System displays user's profile preview
4. Shows user's bio/self-introduction
5. Displays user's room (if public)
6. Mike clicks to join friend's room
```

#### Journey 3: Browsing New Rooms
```
1. User switches to "새로 생성된 방" tab
2. Sees rooms sorted by creation date (newest first)
3. Pagination controls show "1 2 3 ... 10"
4. Each page shows 20 rooms
5. User clicks page 2 to see more
6. Finds interesting new room and joins
```

#### Journey 4: Resetting Room Password
```
1. Emma forgot her room password
2. Goes to room settings
3. Clicks "비밀번호 재발급" button
4. System generates new password
5. Displays new password with copy button
6. Emma saves new password securely
7. Old password immediately invalidated
```

## Requirements

### Functional Requirements

#### FR1: Popular Rooms Display
- **FR1.1**: Display table of rooms sorted by like count (descending)
- **FR1.2**: Show room name, owner info, stats (likes, members)
- **FR1.3**: Include small owner profile image (32x32dp)
- **FR1.4**: Clickable owner profile opens modal with bio
- **FR1.5**: "참여하기" button to join room
- **FR1.6**: Pagination with 20 rooms per page
- **FR1.7**: Real-time like count updates

#### FR2: User ID Search
- **FR2.1**: Search input accepts user ID
- **FR2.2**: Display user profile preview on match
- **FR2.3**: Show user's bio/self-introduction
- **FR2.4**: Display large profile image (120x120dp)
- **FR2.5**: Show user's room if available
- **FR2.6**: Direct join button for user's room
- **FR2.7**: Handle no-results state gracefully

#### FR3: New Rooms Display
- **FR3.1**: Display table sorted by creation date (newest first)
- **FR3.2**: Same table format as popular rooms
- **FR3.3**: Pagination with 20 rooms per page
- **FR3.4**: Indicator for "new" rooms (< 7 days old)
- **FR3.5**: Same profile modal as popular rooms

#### FR4: User Profile Modal
- **FR4.1**: Large circular profile image (120-150dp)
- **FR4.2**: User name prominently displayed
- **FR4.3**: Bio/self-introduction text (scrollable if long)
- **FR4.4**: Placeholder text if no bio set
- **FR4.5**: "방 참여하기" button (disabled if no room)
- **FR4.6**: Close button or swipe-down to dismiss

#### FR5: Room Password Management
- **FR5.1**: "비밀번호 재발급" button in room settings
- **FR5.2**: Confirmation dialog before reset
- **FR5.3**: Generate secure random password
- **FR5.4**: Display new password with copy button
- **FR5.5**: Invalidate old password immediately
- **FR5.6**: Log password reset event for audit
- **FR5.7**: Send notification to room owner

#### FR6: Table Display Format
- **FR6.1**: Columns: Room Name | Owner | Stats | Actions
- **FR6.2**: Responsive design for mobile/tablet
- **FR6.3**: Sort controls for each column
- **FR6.4**: Filter controls (by category, privacy)
- **FR6.5**: Loading states while fetching data
- **FR6.6**: Error states for failed requests

### Non-Functional Requirements

#### NFR1: Performance
- **NFR1.1**: Search results return within 200ms
- **NFR1.2**: Pagination navigation < 100ms
- **NFR1.3**: Profile modal opens < 50ms
- **NFR1.4**: Support 1000+ concurrent users
- **NFR1.5**: Cache popular rooms for 5 minutes

#### NFR2: Security
- **NFR2.1**: Rate limit search to 10 requests/minute/user
- **NFR2.2**: Sanitize all user inputs
- **NFR2.3**: Password reset requires authentication
- **NFR2.4**: Log all password reset events
- **NFR2.5**: HTTPS for all API calls

#### NFR3: Scalability
- **NFR3.1**: Database indices on like_count, created_at
- **NFR3.2**: Pagination backend to handle 100k+ rooms
- **NFR3.3**: CDN for profile images
- **NFR3.4**: Lazy loading for images in tables

#### NFR4: Usability
- **NFR4.1**: Mobile-first responsive design
- **NFR4.2**: Touch targets minimum 44x44dp
- **NFR4.3**: Accessibility labels for screen readers
- **NFR4.4**: Keyboard navigation support
- **NFR4.5**: Clear loading and error states

## Success Criteria

### Metrics & KPIs

#### Primary Metrics
1. **Room Discovery Rate**
   - Target: 60% of new users join a room within first session
   - Measurement: Track join events within 24h of account creation

2. **Search Usage**
   - Target: 40% of users use search at least once per week
   - Measurement: Weekly active search users / WAU

3. **Popular Room Engagement**
   - Target: Popular rooms tab drives 50% of new room joins
   - Measurement: Joins from popular tab / total joins

4. **Password Reset Success**
   - Target: 95% successful password resets
   - Measurement: Successful resets / total reset attempts

#### Secondary Metrics
1. **Profile Modal Engagement**: 30% of users view at least 3 profiles per session
2. **Pagination Usage**: Average 2.5 pages viewed per search session
3. **New Rooms Discovery**: 20% of joins from "new rooms" tab
4. **User Search CTR**: 70% of searches result in profile view

### Acceptance Criteria
- [ ] Popular rooms display with accurate like counts
- [ ] User ID search returns results in < 200ms
- [ ] New rooms tab shows newest rooms first
- [ ] Profile modal displays bio and large image
- [ ] Password reset generates new secure password
- [ ] Pagination works correctly with 20 items per page
- [ ] All tables responsive on mobile devices
- [ ] No security vulnerabilities in password reset
- [ ] 95%+ uptime for search functionality
- [ ] Accessible via keyboard and screen readers

## Technical Architecture

### Frontend Components
1. **RoomSearchScreen.kt**
   - Tab layout: Popular | New | Search
   - Table/List view for rooms
   - Pagination controls
   - Profile modal

2. **UserProfileModal.kt**
   - Large profile image
   - Bio display
   - Join room button

3. **RoomPasswordResetDialog.kt**
   - Confirmation dialog
   - Password display
   - Copy to clipboard

### Backend Endpoints

#### GET /v0.1/rooms/popular
```json
{
  "page": 1,
  "limit": 20,
  "sort": "likes_desc",
  "total": 150,
  "rooms": [
    {
      "id": 123,
      "name": "Travel Enthusiasts",
      "owner": {
        "id": 456,
        "name": "John Doe",
        "profile_image_url": "https://...",
        "bio": "Love exploring new places..."
      },
      "likes_count": 245,
      "members_count": 89,
      "created_at": "2025-11-01T10:00:00Z"
    }
  ]
}
```

#### GET /v0.1/rooms/recent
```json
{
  "page": 1,
  "limit": 20,
  "sort": "created_desc",
  "total": 89,
  "rooms": [...]
}
```

#### GET /v0.1/users/:userId
```json
{
  "id": 456,
  "name": "John Doe",
  "email": "john@example.com",
  "profile_image_url": "https://...",
  "bio": "Travel lover and photographer...",
  "room": {
    "id": 123,
    "name": "Travel Enthusiasts",
    "is_public": true
  }
}
```

#### POST /v0.1/rooms/:roomId/reset-password
```json
Request:
{
  "confirmation": true
}

Response:
{
  "new_password": "xK9#mP2$vL7&",
  "expires_at": null,
  "reset_at": "2025-11-11T03:30:00Z"
}
```

### Database Schema Changes

#### New Indices
```sql
CREATE INDEX idx_rooms_likes_count ON rooms(likes_count DESC);
CREATE INDEX idx_rooms_created_at ON rooms(created_at DESC);
CREATE INDEX idx_users_bio ON users(bio) WHERE bio IS NOT NULL;
```

#### New Table: room_password_resets
```sql
CREATE TABLE room_password_resets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    reset_by_user_id BIGINT NOT NULL,
    old_password_hash VARCHAR(255),
    new_password_hash VARCHAR(255) NOT NULL,
    reset_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    FOREIGN KEY (reset_by_user_id) REFERENCES users(id)
);
```

## Constraints & Assumptions

### Technical Constraints
- Must work with existing room permission system
- Pagination must handle 100k+ rooms efficiently
- Profile images stored in S3 with CDN
- Search must use existing user authentication

### Timeline Constraints
- MVP delivery: 2 weeks from start
- Full feature: 3-4 weeks with testing
- Password reset: Priority for security, week 1

### Resource Constraints
- 1 backend developer
- 1 frontend developer
- QA support available week 3
- Design resources shared with other projects

### Assumptions
1. Users will search primarily by user ID, not by name
2. Popular rooms = sorted by total likes (not like rate)
3. "New" rooms = created within last 7 days
4. Bio field already implemented (VARCHAR 500)
5. Room passwords are hashed and salted
6. All rooms have an owner who can reset password

## Out of Scope

### Explicitly NOT Building
1. **Advanced Search Filters**
   - No keyword search in room names/descriptions
   - No category-based filtering in this release
   - No date range filters

2. **Social Features**
   - No friend system integration
   - No room recommendations based on user behavior
   - No "trending" algorithm (just raw like count)

3. **Password Features**
   - No password expiry
   - No password complexity requirements
   - No "forgot password" email flow
   - No password history tracking

4. **Analytics Dashboard**
   - No admin view of search analytics
   - No heat maps of popular times
   - No conversion funnel tracking

5. **Advanced Pagination**
   - No infinite scroll option
   - No "load more" pattern
   - No customizable page sizes

### Future Considerations
- Machine learning based recommendations
- Advanced search with filters and facets
- Room analytics dashboard for owners
- Social graph integration
- Scheduled password resets
- Password sharing with selected users

## Dependencies

### External Dependencies
1. **S3/CDN**: Profile image storage and delivery
2. **Database**: MySQL 8.0+ for pagination performance
3. **Redis**: Optional caching for popular rooms list
4. **Mobile OS**: Android 8.0+ for frontend features

### Internal Dependencies
1. **Authentication System**: User login and session management
2. **Room System**: Existing room creation and management
3. **Profile System**: Bio field must be implemented first
4. **Permission System**: Room owner verification for password reset

### Team Dependencies
1. **Design Team**: UI mockups for table layout and modals
2. **QA Team**: Test plan for password reset security
3. **DevOps**: Database indices creation during low-traffic window
4. **Security Team**: Review password generation algorithm

## Implementation Phases

### Phase 1: Foundation (Week 1)
- Database schema updates and indices
- Backend API for popular/recent rooms
- User search endpoint
- Basic pagination logic

### Phase 2: Frontend Core (Week 2)
- Room search screen with tabs
- Table layout for popular/new rooms
- User profile modal
- Pagination UI controls

### Phase 3: Password Reset (Week 2-3)
- Backend password reset endpoint
- Frontend reset dialog
- Audit logging
- Security testing

### Phase 4: Polish & Testing (Week 3-4)
- Performance optimization
- Mobile responsive testing
- Accessibility improvements
- Bug fixes and edge cases

## Risk Assessment

### High Risk
1. **Security**: Password reset vulnerability
   - Mitigation: Security review, rate limiting, audit logs

2. **Performance**: Slow pagination with large datasets
   - Mitigation: Database indices, caching, query optimization

### Medium Risk
1. **UX Confusion**: Too many navigation options
   - Mitigation: User testing, clear visual hierarchy

2. **Data Quality**: Incomplete or inappropriate bios
   - Mitigation: Character limit, moderation tools

### Low Risk
1. **Adoption**: Users don't use search feature
   - Mitigation: Analytics tracking, iterate on UX

## Appendix

### Glossary
- **Popular Room**: Room sorted by total like count
- **New Room**: Room created within last 7 days
- **Bio**: User's self-introduction text (max 500 chars)
- **Password Reset**: Generate new random password, invalidate old

### Related Documents
- `/pm:prd-parse profile-management` - User profile features
- `/pm:prd-parse room-wishlist` - Room creation and management
- Backend API Documentation (to be created)

### Open Questions
1. Should popular rooms cache refresh every 5 minutes or real-time?
2. Should user search support partial matches or exact ID only?
3. Should password reset notify all room members?
4. Should we show room preview before joining?

---

**Next Steps:**
1. Design team creates mockups for room search screen
2. Backend team implements database indices
3. Security team reviews password reset flow
4. Run: `/pm:prd-parse room-search-discovery` to create implementation epic
