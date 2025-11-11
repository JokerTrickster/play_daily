#!/bin/bash

# Room Discovery Endpoints Test Script
# Tests GET /v0.1/rooms/popular and GET /v0.1/rooms/recent

BASE_URL="http://localhost:8080"

echo "=========================================="
echo "Room Discovery Endpoints Test"
echo "=========================================="
echo ""

# Test 1: Get Popular Rooms (Default pagination)
echo "Test 1: GET /v0.1/rooms/popular (default: page=1, limit=20)"
curl -s -X GET "$BASE_URL/v0.1/rooms/popular" | jq '.'
echo ""
echo ""

# Test 2: Get Popular Rooms with custom pagination
echo "Test 2: GET /v0.1/rooms/popular?page=1&limit=5"
curl -s -X GET "$BASE_URL/v0.1/rooms/popular?page=1&limit=5" | jq '.'
echo ""
echo ""

# Test 3: Get Recent Rooms (Default pagination)
echo "Test 3: GET /v0.1/rooms/recent (default: page=1, limit=20)"
curl -s -X GET "$BASE_URL/v0.1/rooms/recent" | jq '.'
echo ""
echo ""

# Test 4: Get Recent Rooms with custom pagination
echo "Test 4: GET /v0.1/rooms/recent?page=1&limit=5"
curl -s -X GET "$BASE_URL/v0.1/rooms/recent?page=1&limit=5" | jq '.'
echo ""
echo ""

# Test 5: Test page 2
echo "Test 5: GET /v0.1/rooms/popular?page=2&limit=5"
curl -s -X GET "$BASE_URL/v0.1/rooms/popular?page=2&limit=5" | jq '.'
echo ""
echo ""

# Test 6: Invalid page parameter
echo "Test 6: GET /v0.1/rooms/popular?page=0 (should error)"
curl -s -X GET "$BASE_URL/v0.1/rooms/popular?page=0" | jq '.'
echo ""
echo ""

# Test 7: Invalid limit parameter
echo "Test 7: GET /v0.1/rooms/popular?limit=-1 (should error)"
curl -s -X GET "$BASE_URL/v0.1/rooms/popular?limit=-1" | jq '.'
echo ""
echo ""

# Test 8: Limit exceeds max (should cap at 100)
echo "Test 8: GET /v0.1/rooms/popular?limit=200 (should cap to 100)"
curl -s -X GET "$BASE_URL/v0.1/rooms/popular?limit=200" | jq '.limit'
echo ""
echo ""

echo "=========================================="
echo "Test Complete!"
echo "=========================================="
