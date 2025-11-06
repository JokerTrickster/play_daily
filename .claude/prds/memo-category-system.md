---
name: memo-category-system
description: Replace free-text memo content with multi-select category system for structured feedback
status: backlog
created: 2025-11-05T07:31:51Z
---

# PRD: Memo Category System

## Executive Summary

Transform the memo creation system from free-text content to a structured, multi-select category system. This change enables users to quickly capture their experiences through predefined sentiment categories while maintaining compatibility with map-based and list-based memo creation flows. The new system will provide better data structure for future analytics and improved UX through faster memo creation.

**Key Changes:**
- Replace free-text content with 10 predefined sentiment categories
- Group categories by positive/negative sentiment
- Support unlimited multi-select category choices
- Maintain separate flows for map-based (auto-title) and list-based (manual title) creation
- Complete database restructure with new category tables
- Full backward incompatibility - existing free-text memos will be removed

## Problem Statement

### Current Pain Points
1. **Unstructured Data**: Free-text content makes it difficult to analyze user sentiment and preferences
2. **Slow Input**: Users must type out full descriptions, increasing friction in memo creation
3. **Inconsistent Quality**: User-written content varies in quality and usefulness
4. **Analysis Difficulty**: Cannot easily filter, search, or aggregate memo sentiments

### Why This Change Now
- Users need faster memo creation for quick experience capture
- Product requires structured data for future recommendation features
- Current free-text approach doesn't scale for analytics and insights
- Category-based system aligns with quick-rating use cases

## User Stories

### Primary Persona: Active User
**Name**: 지수 (Jisoo)
**Context**: 20s-30s urban dweller who visits cafes, restaurants, and places frequently
**Goal**: Quickly record experiences without typing lengthy reviews
**Pain Point**: Typing full reviews on mobile is slow and cumbersome

### User Journey 1: Map-Based Memo Creation

**Scenario**: Jisoo visits a new cafe and wants to save her impression

1. **Discovery**: Opens map view, browses nearby places
2. **Selection**: Taps on cafe location pin
3. **Creation Start**: Taps "Create Memo" button
4. **Title Auto-Fill**: Cafe name automatically populated as title (non-editable)
5. **Category Selection**: Views 10 categorized sentiment options
6. **Multi-Select**: Selects "너무 좋았다", "다음에 또 방문할 의향이 있다", "분위기가 좋다"
7. **Additional Info**: Adds photo, selects 5-star rating, confirms location
8. **Completion**: Saves memo in 15 seconds (vs. 2+ minutes for text input)

**Acceptance Criteria**:
- ✅ Title is auto-populated from map selection
- ✅ Title field is read-only when creating from map
- ✅ All 10 categories are visible with clear sentiment indicators
- ✅ User can select multiple categories without limit
- ✅ Selected categories display as visual chips/tags
- ✅ Image, rating, location inputs remain unchanged
- ✅ Memo saves successfully with all selected categories

### User Journey 2: List-Based Memo Creation

**Scenario**: Jisoo wants to add a personal place not on the map

1. **Discovery**: Opens memo list view
2. **Creation Start**: Taps "+" button for new memo
3. **Title Input**: Manually types "우리집 앞 단골 떡볶이"
4. **Category Selection**: Views same 10 sentiment categories
5. **Multi-Select**: Chooses "가성비 좋다", "다음에 또 방문할 의향이 있다"
6. **Additional Info**: Adds photo, 4-star rating, location
7. **Completion**: Saves memo successfully

**Acceptance Criteria**:
- ✅ Title field is editable when creating from list
- ✅ Category selection UI is identical to map-based flow
- ✅ All other fields function the same as map-based creation
- ✅ Clear UI indicator shows this is "list creation mode"

### User Journey 3: Viewing Memo with Categories

**Scenario**: Jisoo reviews her saved memos

1. **List View**: Opens saved memos list
2. **Category Display**: Each memo shows selected categories as colored tags
3. **Filtering**: Taps "Filter" and selects "너무 좋았다" category
4. **Filtered Results**: Views only memos with that category
5. **Detail View**: Taps memo to see full details with all categories highlighted

**Acceptance Criteria**:
- ✅ Categories display as visual tags in list view
- ✅ Positive categories show green/positive colors
- ✅ Negative categories show red/negative colors
- ✅ Filter by category works correctly
- ✅ Multiple category filters work with AND/OR logic
- ✅ Detail view shows all selected categories clearly

## Requirements

### Functional Requirements

#### FR-1: Category System
- **FR-1.1**: System must support exactly 10 predefined categories (fixed, non-customizable)
- **FR-1.2**: Categories must be grouped by sentiment: Positive, Negative, Neutral
- **FR-1.3**: Users can select unlimited number of categories per memo
- **FR-1.4**: Category selection is mandatory (at least 1 category must be selected)
- **FR-1.5**: Categories are presented as checkboxes for multi-select

**Proposed Category List** (10 categories):

**긍정 카테고리 (Positive - 5개)**
1. "너무 좋았다" (Really great)
2. "다음에 또 방문할 의향이 있다" (Would visit again)
3. "가성비 좋다" (Great value)
4. "분위기가 좋다" (Great atmosphere)
5. "음식/서비스가 훌륭하다" (Excellent food/service)

**부정 카테고리 (Negative - 3개)**
6. "완전 최악, 가성비 최악" (Worst value ever)
7. "다시는 안 갈 것 같다" (Won't visit again)
8. "기대 이하였다" (Below expectations)

**중립 카테고리 (Neutral - 2개)**
9. "그냥 무난했다" (Just okay)
10. "특별한 점이 없었다" (Nothing special)

#### FR-2: Memo Creation - Map-Based Flow
- **FR-2.1**: When user creates memo from map selection, title auto-populates with place name
- **FR-2.2**: Title field is read-only (non-editable) in map-based creation
- **FR-2.3**: UI clearly indicates "Map-based creation mode"
- **FR-2.4**: Category selection, image, rating, location inputs function normally

#### FR-3: Memo Creation - List-Based Flow
- **FR-3.1**: When user creates memo from list view, title field is editable text input
- **FR-3.2**: Title is required and cannot be empty
- **FR-3.3**: UI clearly indicates "List-based creation mode"
- **FR-3.4**: All other inputs identical to map-based flow

#### FR-4: Category Display & Filtering
- **FR-4.1**: Memo list view displays selected categories as colored tags/badges
- **FR-4.2**: Positive categories display with green/positive visual styling
- **FR-4.3**: Negative categories display with red/negative visual styling
- **FR-4.4**: Neutral categories display with gray/neutral visual styling
- **FR-4.5**: Users can filter memos by category from list view
- **FR-4.6**: Multiple category filters supported (with clear AND/OR logic)
- **FR-4.7**: Filter state persists during session

#### FR-5: Data Migration
- **FR-5.1**: All existing memos with free-text content will be **deleted**
- **FR-5.2**: System must backup existing memos before deletion
- **FR-5.3**: Database schema updated to remove `content` text field
- **FR-5.4**: No backward compatibility with free-text content

### Non-Functional Requirements

#### NFR-1: Performance
- **NFR-1.1**: Memo creation completes in <2 seconds
- **NFR-1.2**: Category filter applies in <500ms
- **NFR-1.3**: List view renders 50+ memos with categories in <1 second

#### NFR-2: Usability
- **NFR-2.1**: Category selection UI fits on single screen (no scrolling for categories)
- **NFR-2.2**: Touch targets for checkboxes meet minimum 44x44pt accessibility standards
- **NFR-2.3**: Visual distinction between selected/unselected categories is clear
- **NFR-2.4**: Category text is readable at default system font size

#### NFR-3: Data Integrity
- **NFR-3.1**: Selected categories persist correctly on memo save
- **NFR-3.2**: Memo-category relationships maintain referential integrity
- **NFR-3.3**: Category master data is immutable (no accidental deletion)

#### NFR-4: Localization
- **NFR-4.1**: Category text supports Korean language
- **NFR-4.2**: System designed for future multi-language support

## Database Schema

### New Tables

#### Table: `memo_categories`
Stores predefined, immutable category master data.

```sql
CREATE TABLE memo_categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name_ko VARCHAR(100) NOT NULL COMMENT '카테고리 이름 (한국어)',
    sentiment ENUM('positive', 'negative', 'neutral') NOT NULL COMMENT '감정 분류',
    display_order INT NOT NULL COMMENT '표시 순서',
    color_hex VARCHAR(7) NOT NULL COMMENT 'UI 색상 코드 (#RRGGBB)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name_ko (name_ko),
    KEY idx_sentiment (sentiment),
    KEY idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='메모 카테고리 마스터 테이블';
```

**Initial Data**:
```sql
INSERT INTO memo_categories (name_ko, sentiment, display_order, color_hex) VALUES
('너무 좋았다', 'positive', 1, '#10B981'),
('다음에 또 방문할 의향이 있다', 'positive', 2, '#10B981'),
('가성비 좋다', 'positive', 3, '#10B981'),
('분위기가 좋다', 'positive', 4, '#10B981'),
('음식/서비스가 훌륭하다', 'positive', 5, '#10B981'),
('완전 최악, 가성비 최악', 'negative', 6, '#EF4444'),
('다시는 안 갈 것 같다', 'negative', 7, '#EF4444'),
('기대 이하였다', 'negative', 8, '#EF4444'),
('그냥 무난했다', 'neutral', 9, '#6B7280'),
('특별한 점이 없었다', 'neutral', 10, '#6B7280');
```

#### Table: `memo_category_selections`
Junction table for many-to-many relationship between memos and categories.

```sql
CREATE TABLE memo_category_selections (
    id INT PRIMARY KEY AUTO_INCREMENT,
    memo_id INT NOT NULL COMMENT '메모 ID (FK)',
    category_id INT NOT NULL COMMENT '카테고리 ID (FK)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_memo_category (memo_id, category_id),
    FOREIGN KEY (memo_id) REFERENCES memos(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES memo_categories(id) ON DELETE RESTRICT,
    KEY idx_memo_id (memo_id),
    KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='메모-카테고리 연결 테이블';
```

### Modified Tables

#### Table: `memos` (Updated)
Remove free-text `content` field, add creation mode indicator.

```sql
ALTER TABLE memos DROP COLUMN content;

ALTER TABLE memos ADD COLUMN creation_mode ENUM('map', 'list') NOT NULL DEFAULT 'list'
    COMMENT '생성 방식 (map: 지도 기반, list: 리스트 기반)' AFTER title;

ALTER TABLE memos MODIFY COLUMN title VARCHAR(200) NOT NULL
    COMMENT '메모 제목 (지도 선택 시 장소명, 리스트 생성 시 사용자 입력)';
```

### Migration Strategy

**Phase 1: Backup**
```sql
-- Backup existing memos table
CREATE TABLE memos_backup_20251105 AS SELECT * FROM memos;
```

**Phase 2: Data Deletion**
```sql
-- Delete all memos (as per requirement)
TRUNCATE TABLE memos;
```

**Phase 3: Schema Update**
```sql
-- Execute ALTER TABLE statements above
-- Create new tables (memo_categories, memo_category_selections)
-- Insert initial category data
```

**Phase 4: Validation**
```sql
-- Verify table structures
DESCRIBE memos;
DESCRIBE memo_categories;
DESCRIBE memo_category_selections;

-- Verify category data
SELECT * FROM memo_categories ORDER BY display_order;
```

## API Changes

### Backend Changes Required

#### New Endpoints

**GET /api/v1/categories**
- Returns list of all available categories with metadata
- Response includes: id, name, sentiment, color, display_order

```json
{
  "categories": [
    {
      "id": 1,
      "name": "너무 좋았다",
      "sentiment": "positive",
      "color": "#10B981",
      "display_order": 1
    },
    ...
  ]
}
```

**POST /api/v1/memos** (Modified)
- Remove `content` field from request body
- Add `category_ids` array field
- Add `creation_mode` field

```json
{
  "title": "스타벅스 강남점",
  "creation_mode": "map",
  "category_ids": [1, 2, 4],
  "image_url": "https://...",
  "rating": 5,
  "latitude": 37.123,
  "longitude": 127.456,
  "naver_place_url": "https://..."
}
```

**GET /api/v1/memos** (Modified)
- Add optional `category_ids` query parameter for filtering
- Response includes category details for each memo

```json
{
  "memos": [
    {
      "id": 123,
      "title": "스타벅스 강남점",
      "creation_mode": "map",
      "categories": [
        {"id": 1, "name": "너무 좋았다", "sentiment": "positive", "color": "#10B981"},
        {"id": 2, "name": "다음에 또 방문할 의향이 있다", "sentiment": "positive", "color": "#10B981"}
      ],
      "image_url": "https://...",
      "rating": 5,
      "created_at": "2025-11-05T07:30:00Z"
    }
  ]
}
```

**GET /api/v1/memos/:id** (Modified)
- Response includes full category details

#### Modified Business Logic

**Memo Creation Validation**:
- Validate `category_ids` array is not empty (at least 1 category required)
- Validate all `category_ids` exist in `memo_categories` table
- Validate `creation_mode` is either 'map' or 'list'
- If `creation_mode` is 'map', validate `naver_place_url` is present

**Memo Filtering**:
- Support filtering by multiple `category_ids`
- Implement AND logic (memo must have ALL specified categories)
- Future: support OR logic via query parameter

### Frontend Changes Required

#### Android App (Kotlin)

**New Models**:
```kotlin
data class MemoCategory(
    val id: Int,
    val name: String,
    val sentiment: CategorySentiment,
    val color: String,
    val displayOrder: Int
)

enum class CategorySentiment {
    POSITIVE, NEGATIVE, NEUTRAL
}

enum class CreationMode {
    MAP, LIST
}

data class MemoCreateRequest(
    val title: String,
    val creationMode: CreationMode,
    val categoryIds: List<Int>,
    val imageUrl: String?,
    val rating: Int,
    val latitude: Double?,
    val longitude: Double?,
    val naverPlaceUrl: String?
)
```

**New UI Components**:
1. **CategorySelectionView**: Checkbox grid displaying all 10 categories
2. **CategoryTagView**: Colored tag/chip for displaying selected categories
3. **CategoryFilterBottomSheet**: Filter UI for memo list

**Modified Screens**:
1. **MemoCreateScreen (Map Mode)**:
   - Remove content text input
   - Add CategorySelectionView
   - Make title field read-only
   - Add visual indicator for "Map Mode"

2. **MemoCreateScreen (List Mode)**:
   - Remove content text input
   - Add CategorySelectionView
   - Keep title field editable
   - Add visual indicator for "List Mode"

3. **MemoListScreen**:
   - Display CategoryTagView for each memo
   - Add filter button opening CategoryFilterBottomSheet
   - Support category-based filtering

4. **MemoDetailScreen**:
   - Display all selected categories as tags
   - Remove free-text content display

## Success Criteria

### Quantitative Metrics

1. **Creation Speed**: Average memo creation time reduces from 120s to <30s (75% improvement)
2. **Adoption Rate**: 80%+ of new memos use category system within first week
3. **Category Usage**: All 10 categories used at least 5% of the time (no "dead" categories)
4. **Multi-Select Rate**: 70%+ of memos have 2+ categories selected (validates multi-select value)
5. **Filter Usage**: 30%+ of users use category filter within first month

### Qualitative Metrics

1. **User Feedback**: Positive sentiment in user interviews about faster creation
2. **Data Quality**: Product team can analyze sentiment trends across memos
3. **UX Clarity**: Users understand difference between map/list creation modes
4. **Visual Design**: Categories display clearly without cluttering UI

### Launch Criteria

- ✅ All database migrations completed successfully
- ✅ Backend API endpoints tested and validated
- ✅ Android app UI implemented per design specs
- ✅ Category selection supports all interaction patterns (tap, multi-select, deselect)
- ✅ Filtering works correctly with multiple categories
- ✅ No crashes or data loss in testing
- ✅ Performance meets NFR targets (<2s creation, <500ms filter)

## Constraints & Assumptions

### Technical Constraints
- **Backend**: Go language, existing REST API architecture
- **Frontend**: Android Kotlin, Jetpack Compose
- **Database**: MySQL, must maintain ACID properties
- **Breaking Change**: This is a breaking change requiring data deletion

### Resource Constraints
- **Timeline**: Expected 2-week development cycle (1 week backend, 1 week frontend)
- **Team**: 1 backend developer, 1 Android developer
- **Testing**: Manual testing only (no automated test suite)

### Assumptions
- Users prefer faster, structured input over free-text flexibility
- 10 categories cover 80%+ of common user sentiments
- Category-based data enables future recommendation features
- Users accept loss of existing free-text memos for improved system
- Korean language support is sufficient for MVP (no English needed)

## Out of Scope

### Explicitly NOT Building

1. **Custom Categories**: Users cannot create their own categories
2. **Category Analytics Dashboard**: No analytics UI for category popularity
3. **AI Content Analysis**: No AI to suggest categories based on image/text
4. **Category Editing**: Cannot edit/delete/reorder predefined categories
5. **Bulk Edit**: Cannot bulk-change categories on existing memos
6. **Export Feature**: No CSV/Excel export with category data
7. **Social Features**: No sharing memos with category tags
8. **Search by Category**: Search functionality remains unchanged (title/location only)
9. **Category Recommendations**: No "users like you also selected..." suggestions
10. **Multi-Language**: English/other language support deferred to v2

### Future Considerations (Post-MVP)
- Category usage analytics for product insights
- Smart category suggestions based on location/rating
- User-customizable category colors
- Category-based social features (find memos with same categories)
- Export memos with category data

## Dependencies

### Internal Dependencies
- **Database Team**: Must review and approve schema changes
- **API Team**: Must implement new endpoints and modify existing ones
- **Android Team**: Must implement new UI components and update flows
- **QA Team**: Must test all flows thoroughly before release

### External Dependencies
- **None**: This feature has no external API or service dependencies

### Technical Dependencies
- **Jetpack Compose**: Android UI requires Compose library
- **Database Migration Tool**: Need migration script runner for schema changes
- **Color Library**: Android needs color parsing for category color codes

## Risks & Mitigation

### High-Risk Areas

**Risk 1: User Backlash from Data Loss**
- **Impact**: Users lose all existing memo content
- **Probability**: High
- **Mitigation**:
  - Provide 2-week advance notice via in-app notification
  - Offer manual export option before deletion
  - Communicate clear value proposition of new system
  - Consider soft launch to subset of users first

**Risk 2: Category Set Incomplete**
- **Impact**: Users cannot express important sentiments
- **Probability**: Medium
- **Mitigation**:
  - User research to validate 10 categories cover common cases
  - Monitor user feedback in first 2 weeks
  - Plan fast-follow update if critical categories missing

**Risk 3: UI Complexity with 10 Checkboxes**
- **Impact**: Creation screen feels cluttered or overwhelming
- **Probability**: Medium
- **Mitigation**:
  - Use clear visual grouping by sentiment
  - Design mobile-first with optimal spacing
  - A/B test checkbox vs. chip-based selection UI

**Risk 4: Performance Degradation with Many Categories**
- **Impact**: List view slow with many memos and category tags
- **Probability**: Low
- **Mitigation**:
  - Implement pagination/lazy loading for memo list
  - Cache category master data in app
  - Optimize SQL queries with proper indexing

**Risk 5: Migration Failure**
- **Impact**: Data loss or system downtime
- **Probability**: Low
- **Mitigation**:
  - Complete database backup before migration
  - Test migration on staging environment
  - Plan rollback procedure
  - Schedule during low-traffic maintenance window

## Timeline & Milestones

### Phase 1: Backend Development (Week 1)
**Days 1-2**:
- Database schema design review
- Create migration scripts
- Execute migration on staging DB

**Days 3-4**:
- Implement new API endpoints (GET /categories, modified POST /memos)
- Update memo creation/retrieval logic
- Add category validation

**Day 5**:
- Backend testing and bug fixes
- API documentation updates

### Phase 2: Frontend Development (Week 2)
**Days 1-2**:
- Design and implement CategorySelectionView UI component
- Design and implement CategoryTagView for list display
- Create category filter UI

**Days 3-4**:
- Update MemoCreateScreen for both map/list modes
- Update MemoListScreen with category tags and filtering
- Update MemoDetailScreen with category display

**Day 5**:
- Integration testing
- UI polish and bug fixes

### Phase 3: Testing & Launch (Week 3)
**Days 1-2**:
- Comprehensive QA testing (all flows, edge cases)
- Fix critical bugs

**Day 3**:
- Staging environment validation
- Production deployment preparation

**Day 4**:
- Production database migration
- Production app release

**Day 5**:
- Monitor for issues
- Collect user feedback

## Appendix

### Design References
- Checkbox selection: Material Design 3 guidelines
- Category tags: Chip component from Material Design
- Color scheme: Tailwind CSS color palette (Green 500, Red 500, Gray 500)

### Related Documents
- Original PRD: `.claude/prds/daily-memo-app.md`
- Room Wishlist Feature: `.claude/prds/room-wishlist.md`
- Map Enhancement: `.claude/prds/map-ui-enhancement.md`

### Open Questions
1. Should we add a "None of the above" neutral option?
2. Should category selection be mandatory or optional?
   - **Decision**: Mandatory (at least 1 category required)
3. Should we track category selection timestamps for analytics?
   - **Decision**: Not for MVP, defer to v2
4. Should negative categories require confirmation?
   - **Decision**: No, trust user input without confirmation dialogs

### Approval & Sign-off
- [ ] Product Manager: ___________________ Date: ___________
- [ ] Engineering Lead: __________________ Date: ___________
- [ ] Design Lead: ______________________ Date: ___________
- [ ] QA Lead: _________________________ Date: ___________
