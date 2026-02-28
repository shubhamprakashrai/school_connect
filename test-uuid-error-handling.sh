# ============================================
# Test Improved UUID Error Handling
# ============================================

# Base URL
BASE_URL="http://localhost:8080/api"
AUTH_TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJ0ZW5hbnRJZCI6IlNNMDAwMDEiLCJzdHVkZW50TG9naW5SZXF1aXJlZCI6ZmFsc2UsInVzZXJuYW1lIjoiU00wMDAwMTAwMDAxIiwic3ViIjoiU00wMDAwMTAwMDAxIiwiaWF0IjoxNzcyMTgyODg1LCJleHAiOjE3NzIyNjkyODV9.OyWv15o7jilp3wasb2sID-KTSiDpJncx5ioY3mUrUtg"

echo "=== 1. Test with Invalid UUID Format (should show proper error message) ==="
curl --location "${BASE_URL}/teachers/bulk-attendance" \
--header "Authorization: Bearer ${AUTH_TOKEN}" \
--header "Content-Type: application/json" \
--data '{
    "attendanceDate": "2024-01-15",
    "batchReference": "INVALID-UUID-FORMAT-TEST",
    "continueOnError": true,
    "attendanceRecords": [
      {
        "teacherId": "uuid-of-teacher-here",
        "status": "PRESENT",
        "remarks": "Testing invalid UUID format"
      }
    ]
  }' | jq '.'

echo ""
echo "=== 2. Test with Another Invalid UUID Format ==="
curl --location "${BASE_URL}/teachers/bulk-attendance" \
--header "Authorization: Bearer ${AUTH_TOKEN}" \
--header "Content-Type: application/json" \
--data '{
    "attendanceDate": "2024-01-15",
    "batchReference": "INVALID-UUID-FORMAT-TEST-2",
    "continueOnError": true,
    "attendanceRecords": [
      {
        "teacherId": "invalid-teacher-uuid",
        "status": "PRESENT",
        "remarks": "Testing another invalid UUID format"
      }
    ]
  }' | jq '.'

echo ""
echo "=== 3. Test with Malformed UUID (36 chars but wrong format) ==="
curl --location "${BASE_URL}/teachers/bulk-attendance" \
--header "Authorization: Bearer ${AUTH_TOKEN}" \
--header "Content-Type: application/json" \
--data '{
    "attendanceDate": "2024-01-15",
    "batchReference": "MALFORMED-UUID-TEST",
    "continueOnError": true,
    "attendanceRecords": [
      {
        "teacherId": "invalid-uuid-format-here-12345",
        "status": "PRESENT",
        "remarks": "Testing malformed UUID"
      }
    ]
  }' | jq '.'

echo ""
echo "=== 4. Test with Valid UUID but Non-existent Teacher ==="
curl --location "${BASE_URL}/teachers/bulk-attendance" \
--header "Authorization: Bearer ${AUTH_TOKEN}" \
--header "Content-Type: application/json" \
--data '{
    "attendanceDate": "2024-01-15",
    "batchReference": "VALID-UUID-NONEXISTENT-TEACHER",
    "continueOnError": true,
    "attendanceRecords": [
      {
        "teacherId": "550e8400-e29b-41d4-a716-446655440000",
        "status": "PRESENT",
        "remarks": "Testing valid UUID but non-existent teacher"
      }
    ]
  }' | jq '.'

echo ""
echo "=== 5. Test Validation Only (should show same detailed errors) ==="
curl --location "${BASE_URL}/teachers/bulk-attendance/validate" \
--header "Authorization: Bearer ${AUTH_TOKEN}" \
--header "Content-Type: application/json" \
--data '{
    "attendanceDate": "2024-01-15",
    "batchReference": "VALIDATION-UUID-TEST",
    "continueOnError": true,
    "attendanceRecords": [
      {
        "teacherId": "uuid-of-teacher-here",
        "status": "PRESENT",
        "remarks": "Validation test with invalid UUID"
      },
      {
        "teacherId": "550e8400-e29b-41d4-a716-446655440001",
        "status": "PRESENT",
        "remarks": "Validation test with valid UUID but non-existent teacher"
      }
    ]
  }' | jq '.'
