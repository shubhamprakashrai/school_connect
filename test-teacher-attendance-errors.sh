# ============================================
# Test Teacher Attendance API with Proper Error Handling
# ============================================

# Base URL
BASE_URL="http://localhost:8080/api"
AUTH_TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJ0ZW5hbnRJZCI6IlNNMDAwMDEiLCJzdHVkZW50TG9naW5SZXF1aXJlZCI6ZmFsc2UsInVzZXJuYW1lIjoiU00wMDAwMTAwMDAxIiwic3ViIjoiU00wMDAwMTAwMDAxIiwiaWF0IjoxNzcyMTgyODg1LCJleHAiOjE3NzIyNjkyODV9.OyWv15o7jilp3wasb2sID-KTSiDpJncx5ioY3mUrUtg"

echo "=== 1. Test with Invalid Teacher UUID (should show proper error) ==="
curl --location "${BASE_URL}/teachers/bulk-attendance" \
--header "Authorization: Bearer ${AUTH_TOKEN}" \
--header "Content-Type: application/json" \
--data '{
    "attendanceDate": "2024-01-15",
    "batchReference": "INVALID-TEACHER-TEST",
    "continueOnError": true,
    "attendanceRecords": [
      {
        "teacherId": "550e8400-e29b-41d4-a716-446655440000",
        "status": "PRESENT",
        "remarks": "Test with invalid UUID"
      }
    ]
  }' | jq '.'

echo ""
echo "=== 2. Test with Missing Required Fields (should show validation errors) ==="
curl --location "${BASE_URL}/teachers/bulk-attendance" \
--header "Authorization: Bearer ${AUTH_TOKEN}" \
--header "Content-Type: application/json" \
--data '{
    "attendanceDate": "2024-01-15",
    "batchReference": "MISSING-FIELDS-TEST",
    "continueOnError": true,
    "attendanceRecords": [
      {
        "teacherId": null,
        "status": null,
        "remarks": "Missing required fields test"
      }
    ]
  }' | jq '.'

echo ""
echo "=== 3. Test Template (should work) ==="
curl --location "${BASE_URL}/teachers/bulk-attendance/template" \
--header "Authorization: Bearer ${AUTH_TOKEN}" \
--header "Content-Type: application/json" | jq '.'

echo ""
echo "=== 4. Test Validation Only (should show detailed errors) ==="
curl --location "${BASE_URL}/teachers/bulk-attendance/validate" \
--header "Authorization: Bearer ${AUTH_TOKEN}" \
--header "Content-Type: application/json" \
--data '{
    "attendanceDate": "2024-01-15",
    "batchReference": "VALIDATION-TEST",
    "continueOnError": true,
    "attendanceRecords": [
      {
        "teacherId": "invalid-uuid-format",
        "status": "PRESENT",
        "remarks": "Invalid UUID format test"
      },
      {
        "teacherId": "550e8400-e29b-41d4-a716-446655440001",
        "status": "PRESENT",
        "remarks": "Valid UUID but teacher not found"
      }
    ]
  }' | jq '.'
