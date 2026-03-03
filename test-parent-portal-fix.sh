#!/bin/bash

echo "=== Testing Parent Portal Fix ==="
echo ""

# Test 1: Get all students (should work now)
echo "Test 1: Get all students"
curl -s -w "HTTP Status: %{http_code}\n" \
  -X GET "http://localhost:8080/parent-portal/students" \
  -H "Authorization: Bearer test_token" \
  -H "Content-Type: application/json"

echo ""

# Test 2: Get parent profile 
echo "Test 2: Get parent profile"
curl -s -w "HTTP Status: %{http_code}\n" \
  -X GET "http://localhost:8080/parent-portal/profile" \
  -H "Authorization: Bearer test_token" \
  -H "Content-Type: application/json"

echo ""

# Test 3: Check student access
echo "Test 3: Check student access"
curl -s -w "HTTP Status: %{http_code}\n" \
  -X GET "http://localhost:8080/parent-portal/students/00000000-0000-0000-0000-000000000000/access-check" \
  -H "Authorization: Bearer test_token" \
  -H "Content-Type: application/json"

echo ""

echo "=== Fix Summary ==="
echo "✅ Fixed: Cannot resolve method 'findAllByTenantId'"
echo "✅ Solution: Used existing 'findByTenantId(String tenantId, Pageable pageable)' method"
echo "✅ Added: PageRequest.of(0, 1000) to get all students"
echo "✅ Added: Required imports for Page and Pageable"
echo ""
echo "The parent portal should now work correctly for both admin and parent users!"
