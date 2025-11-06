# Category API Testing Guide

## Issue #43: Backend Memo API Updates for Categories

### Changes Implemented

1. **Request Model Updates** (`features/memo/model/request/createMemo.go`)
   - Added `CreationMode string` field with validation (`map` or `list`)
   - Added `CategoryIDs []uint` field for category selection

2. **Response Model Updates** (`features/memo/model/response/memo.go`)
   - Added `ResCategory` struct with fields: `id`, `name`, `sentiment`, `color`, `display_order`
   - Added `Categories []ResCategory` array to `ResMemo` response

3. **Validation Logic** (`features/memo/model/request/validation.go`)
   - `ValidateCategoryIDs()`: Ensures at least 1 category is selected and all category IDs exist
   - `ValidateCreateMemo()`: Validates entire request including categories and business fields

4. **Repository Layer** (`features/memo/repository/`)
   - `createMemoRepository.go`: Added `CreateWithCategories()` method using database transactions
   - `getMemoRepository.go`: Added `Preload("Categories")` to eagerly load category data
   - Interface updated to include `CreateWithCategories()` method

5. **Use Case Layer** (`features/memo/usecase/`)
   - `createMemoUseCase.go`: Updated to call `CreateWithCategories()` with category IDs
   - `usecase.go`: Updated `convertMemoToResponse()` to convert category data

6. **Handler Layer** (`features/memo/handler/createMemoHandler.go`)
   - Added parsing for `creation_mode` form field (defaults to "list")
   - Added parsing for `category_ids` form field (JSON array format)
   - Added validation call before creating memo
   - Updated Swagger documentation

### Test Instructions

#### Prerequisites
1. Start the backend server
2. Obtain valid authentication token via `/v0.1/auth/signin`
3. Get available category IDs via `/v0.1/categories`

#### Test Case 1: Create Memo with Categories

```bash
# Get categories
curl -X GET "http://13.203.37.93:7001/v0.1/categories"

# Create memo with categories (multipart/form-data)
curl -X POST "http://13.203.37.93:7001/v0.1/memo" \
  -H "tkn: YOUR_AUTH_TOKEN" \
  -F "title=Test Restaurant Visit" \
  -F "creation_mode=map" \
  -F "category_ids=[1,2,3]" \
  -F "rating=5" \
  -F "latitude=37.5665" \
  -F "longitude=126.9780" \
  -F "location_name=Seoul Tower" \
  -F "business_name=Seoul Tower Restaurant" \
  -F "naver_place_url=https://map.naver.com/example"

# Expected Response:
{
  "id": <memo_id>,
  "title": "Test Restaurant Visit",
  "creation_mode": "map",
  "categories": [
    {"id": 1, "name": "너무 좋았다", "sentiment": "positive", "color": "#10B981", "display_order": 1},
    {"id": 2, "name": "다음에도 또 방문할 의향이 있다", "sentiment": "positive", "color": "#10B981", "display_order": 2},
    {"id": 3, "name": "가성비 좋다", "sentiment": "positive", "color": "#10B981", "display_order": 3}
  ],
  "rating": 5,
  "latitude": 37.5665,
  "longitude": 126.9780,
  "location_name": "Seoul Tower",
  "business_name": "Seoul Tower Restaurant",
  "naver_place_url": "https://map.naver.com/example",
  ...
}
```

#### Test Case 2: Get Memo with Categories

```bash
# Get specific memo
curl -X GET "http://13.203.37.93:7001/v0.1/memo/<memo_id>" \
  -H "tkn: YOUR_AUTH_TOKEN"

# Expected Response should include categories array
```

#### Test Case 3: Get Memo List with Categories

```bash
# Get all memos
curl -X GET "http://13.203.37.93:7001/v0.1/memo" \
  -H "tkn: YOUR_AUTH_TOKEN"

# Each memo in the list should include categories array
```

#### Test Case 4: Validation Tests

```bash
# Test 1: Missing category_ids (should fail)
curl -X POST "http://13.203.37.93:7001/v0.1/memo" \
  -H "tkn: YOUR_AUTH_TOKEN" \
  -F "title=Test" \
  -F "creation_mode=list"
# Expected: {"error": "at least one category must be selected"}

# Test 2: Invalid category IDs (should fail)
curl -X POST "http://13.203.37.93:7001/v0.1/memo" \
  -H "tkn: YOUR_AUTH_TOKEN" \
  -F "title=Test" \
  -F "creation_mode=list" \
  -F "category_ids=[999,1000]"
# Expected: {"error": "invalid category_ids: some categories do not exist"}

# Test 3: Invalid creation_mode (should fail)
curl -X POST "http://13.203.37.93:7001/v0.1/memo" \
  -H "tkn: YOUR_AUTH_TOKEN" \
  -F "title=Test" \
  -F "creation_mode=invalid" \
  -F "category_ids=[1,2]"
# Expected: validation error for creation_mode

# Test 4: Map mode without naver_place_url (should fail)
curl -X POST "http://13.203.37.93:7001/v0.1/memo" \
  -H "tkn: YOUR_AUTH_TOKEN" \
  -F "title=Test" \
  -F "creation_mode=map" \
  -F "category_ids=[1,2]"
# Expected: {"error": "naver_place_url is required for map-based creation"}
```

### Compilation Status
✅ Code compiled successfully with no errors

### Database Schema
The implementation uses the existing `memo_category_selections` junction table for many-to-many relationships:
- `memo_id` (FK to memos table)
- `category_id` (FK to memo_categories table)
- Managed automatically by GORM with `many2many:memo_category_selections` tag

### Backward Compatibility
- `content` field marked as Deprecated but still available
- `category` field (single string) marked as Deprecated
- New fields added: `creation_mode`, `category_ids`, `categories` array
