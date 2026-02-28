# ============================================
# Bulk Teacher Attendance API - cURL Commands
# ============================================

# Base URL (update with your actual server URL)
BASE_URL="http://localhost:8080/api"
AUTH_TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJ0ZW5hbnRJZCI6IlNNMDAwMDEiLCJzdHVkZW50TG9naW5SZXF1aXJlZCI6ZmFsc2UsInVzZXJuYW1lIjoiU00wMDAwMTAwMDAxIiwic3ViIjoiU00wMDAwMTAwMDAxIiwiaWF0IjoxNzcyMTgyODg1LCJleHAiOjE3NzIyNjkyODV9.OyWv15o7jilp3wasb2sID-KTSiDpJncx5ioY3mUrUtg"

# ============================================
# 1. GET BULK TEACHER ATTENDANCE TEMPLATE
# ============================================
echo "=== Getting Bulk Teacher Attendance Template ==="
curl -X GET "${BASE_URL}/teachers/bulk-attendance/template" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  | jq '.'

# ============================================
# 2. GET TEACHER ATTENDANCE RECORDS
# ============================================
echo "=== Getting Teacher Attendance Records ==="
curl -X GET "${BASE_URL}/teachers/bulk-attendance?date=2024-01-15&teacherId=uuid-here" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  | jq '.'

# ============================================
# 3. VALIDATE BULK TEACHER ATTENDANCE DATA (without creating)
# ============================================
echo "=== Validating Bulk Teacher Attendance Data ==="
curl -X POST "${BASE_URL}/teachers/bulk-attendance/validate" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "attendanceDate": "2024-01-15",
    "batchReference": "TEACHER-ATTENDANCE-VALIDATION-001",
    "continueOnError": true,
    "attendanceRecords": [
      {
        "teacherId": "uuid-of-teacher-here",
        "status": "PRESENT",
        "remarks": "On time",
        "checkInTime": "08:30",
        "checkOutTime": "16:30",
        "isHalfDay": false
      },
      {
        "teacherId": "uuid-of-teacher-here-2",
        "status": "LATE",
        "remarks": "Late by 15 minutes",
        "checkInTime": "08:45",
        "checkOutTime": "16:30",
        "isHalfDay": false
      },
      {
        "teacherId": "uuid-of-teacher-here-3",
        "status": "ABSENT",
        "remarks": "Sick leave",
        "isHalfDay": false
      }
    ]
  }' \
  | jq '.'

# ============================================
# 4. MARK BULK TEACHER ATTENDANCE (actual creation)
# ============================================
echo "=== Marking Bulk Teacher Attendance ==="
curl -X POST "${BASE_URL}/teachers/bulk-attendance" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "attendanceDate": "2024-01-15",
    "batchReference": "TEACHER-ATTENDANCE-001",
    "continueOnError": true,
    "attendanceRecords": [
      {
        "teacherId": "uuid-of-teacher-here",
        "status": "PRESENT",
        "remarks": "On time",
        "checkInTime": "08:30",
        "checkOutTime": "16:30",
        "isHalfDay": false
      },
      {
        "teacherId": "uuid-of-teacher-here-2",
        "status": "LATE",
        "remarks": "Late by 15 minutes",
        "checkInTime": "08:45",
        "checkOutTime": "16:30",
        "isHalfDay": false
      },
      {
        "teacherId": "uuid-of-teacher-here-3",
        "status": "ABSENT",
        "remarks": "Sick leave",
        "isHalfDay": false
      },
      {
        "teacherId": "uuid-of-teacher-here-4",
        "status": "HALF_DAY",
        "remarks": "Medical appointment",
        "checkInTime": "08:30",
        "checkOutTime": "12:30",
        "isHalfDay": true,
        "halfDayType": "MORNING"
      },
      {
        "teacherId": "uuid-of-teacher-here-5",
        "status": "EXCUSED",
        "remarks": "Official duty",
        "isHalfDay": false
      }
    ]
  }' \
  | jq '.'

# ============================================
# 5. UPDATE SINGLE TEACHER ATTENDANCE
# ============================================
echo "=== Updating Single Teacher Attendance ==="
curl -X PUT "${BASE_URL}/teachers/bulk-attendance/uuid-of-attendance-record-here" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "attendanceDate": "2024-01-15",
    "status": "PRESENT",
    "remarks": "Updated attendance",
    "checkInTime": "08:45",
    "checkOutTime": "16:30",
    "isHalfDay": false
  }' \
  | jq '.'

# ============================================
# 6. DELETE SINGLE TEACHER ATTENDANCE
# ============================================
echo "=== Deleting Single Teacher Attendance ==="
curl -X DELETE "${BASE_URL}/teachers/bulk-attendance/uuid-of-attendance-record-here" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  | jq '.'

# ============================================
# 7. BULK ATTENDANCE WITH DUPLICATE TEACHER (for error testing)
# ============================================
echo "=== Testing Duplicate Teacher Attendance Error Handling ==="
curl -X POST "${BASE_URL}/teachers/bulk-attendance" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "attendanceDate": "2024-01-15",
    "batchReference": "DUPLICATE-TEACHER-TEST-001",
    "continueOnError": true,
    "attendanceRecords": [
      {
        "teacherId": "uuid-of-teacher-here",
        "status": "PRESENT",
        "remarks": "Duplicate test"
      }
    ]
  }' \
  | jq '.'

# ============================================
# 8. BULK ATTENDANCE WITH INVALID TEACHER ID (for validation testing)
# ============================================
echo "=== Testing Invalid Teacher ID Validation ==="
curl -X POST "${BASE_URL}/teachers/bulk-attendance" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "attendanceDate": "2024-01-15",
    "batchReference": "INVALID-TEACHER-TEST-001",
    "continueOnError": true,
    "attendanceRecords": [
      {
        "teacherId": "invalid-teacher-uuid",
        "status": "PRESENT",
        "remarks": "Invalid teacher test"
      }
    ]
  }' \
  | jq '.'

# ============================================
# 9. TEST MISSING REQUEST BODY (should return 400, not 500)
# ============================================
echo "=== Testing Missing Request Body (should return 400) ==="
curl -X POST "${BASE_URL}/teachers/bulk-attendance" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  | jq '.'

# ============================================
# 10. TEST MALFORMED JSON (should return 400, not 500)
# ============================================
echo "=== Testing Malformed JSON (should return 400) ==="
curl -X POST "${BASE_URL}/teachers/bulk-attendance" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "attendanceDate": "2024-01-15",
    "batchReference": "MALFORMED-JSON-TEST",
    "continueOnError": true,
    "attendanceRecords": [
      {
        "teacherId": "uuid-of-teacher-here",
        "status": "PRESENT",
        "remarks": "Malformed test"
      }
    ]
  }' \
  | jq '.'

# ============================================
# 11. TEST MISSING REQUIRED FIELDS (validation testing)
# ============================================
echo "=== Testing Missing Required Fields Validation ==="
curl -X POST "${BASE_URL}/teachers/bulk-attendance" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "attendanceDate": null,
    "batchReference": "MISSING-FIELDS-TEST",
    "continueOnError": true,
    "attendanceRecords": [
      {
        "teacherId": null,
        "status": null,
        "remarks": "Missing fields test"
      }
    ]
  }' \
  | jq '.'

echo "============================================"
echo "All Bulk Teacher Attendance cURL commands completed!"
echo "============================================"