# ============================================
# Bulk Student Upload API - cURL Commands
# ============================================

# Base URL (update with your actual server URL)
BASE_URL="http://localhost:8080/api"
AUTH_TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJ0ZW5hbnRJZCI6IlNNMDAwMDEiLCJzdHVkZW50TG9naW5SZXF1aXJlZCI6ZmFsc2UsInVzZXJuYW1lIjoiU00wMDAwMTAwMDAxIiwic3ViIjoiU00wMDAwMTAwMDAxIiwiaWF0IjoxNzcyMTgyODg1LCJleHAiOjE3NzIyNjkyODV9.OyWv15o7jilp3wasb2sID-KTSiDpJncx5ioY3mUrUtg"

# ============================================
# 1. GET BULK UPLOAD TEMPLATE
# ============================================
echo "=== Getting Bulk Upload Template ==="
curl -X GET "${BASE_URL}/students/bulk/template" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  | jq '.'

# ============================================
# 2. VALIDATE BULK STUDENT DATA (without creating)
# ============================================
echo "=== Validating Bulk Student Data ==="
curl -X POST "${BASE_URL}/students/bulk/validate" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "students": [
      {
        "userRequest": {
          "firstName": "John",
          "middleName": "Michael",
          "lastName": "Doe",
          "email": "john.doe@example.com",
          "phone": "+1234567890"
        },
        "dateOfBirth": "2010-05-15",
        "gender": "MALE",
        "address": "123 Main Street",
        "city": "New York",
        "state": "NY",
        "country": "USA",
        "postalCode": "10001",
        "schoolClass": {
          "id": "uuid-of-school-class-here"
        },
        "section": {
          "id": "uuid-of-section-here"
        },
        "admissionDate": "2024-01-01",
        "previousSchool": "Previous School Name",
        "fatherInfo": {
          "name": "Father Name",
          "occupation": "Engineer",
          "phone": "+1234567891",
          "email": "father@example.com"
        },
        "motherInfo": {
          "name": "Mother Name",
          "occupation": "Teacher",
          "phone": "+1234567892",
          "email": "mother@example.com"
        },
        "emergencyContact": {
          "name": "Emergency Contact",
          "relation": "Guardian",
          "phone": "+1234567893"
        },
        "createUserAccount": true
      },
      {
        "userRequest": {
          "firstName": "Jane",
          "middleName": "Elizabeth",
          "lastName": "Smith",
          "email": "jane.smith@example.com",
          "phone": "+1234567894"
        },
        "dateOfBirth": "2010-08-22",
        "gender": "FEMALE",
        "address": "456 Oak Avenue",
        "city": "Los Angeles",
        "state": "CA",
        "country": "USA",
        "postalCode": "90001",
        "schoolClass": {
          "id": "uuid-of-school-class-here"
        },
        "section": {
          "id": "uuid-of-section-here"
        },
        "admissionDate": "2024-01-01",
        "fatherInfo": {
          "name": "Father Name",
          "occupation": "Doctor",
          "phone": "+1234567895",
          "email": "father.smith@example.com"
        },
        "emergencyContact": {
          "name": "Emergency Contact",
          "relation": "Mother",
          "phone": "+1234567896"
        },
        "createUserAccount": true
      }
    ],
    "batchReference": "VALIDATION-TEST-001",
    "continueOnError": true
  }' \
  | jq '.'

# ============================================
# 3. CREATE BULK STUDENTS (actual creation)
# ============================================
echo "=== Creating Bulk Students ==="
curl -X POST "${BASE_URL}/students/bulk" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "students": [
      {
        "userRequest": {
          "firstName": "Alice",
          "middleName": "Marie",
          "lastName": "Johnson",
          "email": "alice.johnson@example.com",
          "phone": "+1234567897"
        },
        "dateOfBirth": "2010-03-10",
        "gender": "FEMALE",
        "address": "789 Pine Street",
        "city": "Chicago",
        "state": "IL",
        "country": "USA",
        "postalCode": "60007",
        "schoolClass": {
          "id": "uuid-of-school-class-here"
        },
        "section": {
          "id": "uuid-of-section-here"
        },
        "admissionDate": "2024-01-01",
        "previousSchool": "Previous Elementary School",
        "fatherInfo": {
          "name": "Robert Johnson",
          "occupation": "Software Engineer",
          "phone": "+1234567898",
          "email": "robert.johnson@example.com"
        },
        "motherInfo": {
          "name": "Mary Johnson",
          "occupation": "Nurse",
          "phone": "+1234567899",
          "email": "mary.johnson@example.com"
        },
        "guardianInfo": {
          "name": "Grandparent Guardian",
          "phone": "+1234567900",
          "email": "guardian@example.com"
        },
        "emergencyContact": {
          "name": "Mary Johnson",
          "relation": "Mother",
          "phone": "+1234567899"
        },
        "createUserAccount": true
      },
      {
        "userRequest": {
          "firstName": "Bob",
          "lastName": "Williams",
          "email": "bob.williams@example.com",
          "phone": "+1234567901"
        },
        "dateOfBirth": "2010-07-25",
        "gender": "MALE",
        "address": "321 Elm Road",
        "city": "Houston",
        "state": "TX",
        "country": "USA",
        "postalCode": "77001",
        "schoolClass": {
          "id": "uuid-of-school-class-here"
        },
        "section": {
          "id": "uuid-of-section-here"
        },
        "admissionDate": "2024-01-01",
        "fatherInfo": {
          "name": "David Williams",
          "occupation": "Teacher",
          "phone": "+1234567902",
          "email": "david.williams@example.com"
        },
        "emergencyContact": {
          "name": "David Williams",
          "relation": "Father",
          "phone": "+1234567902"
        },
        "createUserAccount": true
      },
      {
        "userRequest": {
          "firstName": "Carol",
          "lastName": "Brown",
          "email": "carol.brown@example.com",
          "phone": "+1234567903"
        },
        "dateOfBirth": "2010-11-30",
        "gender": "FEMALE",
        "address": "654 Maple Drive",
        "city": "Phoenix",
        "state": "AZ",
        "country": "USA",
        "postalCode": "85001",
        "schoolClass": {
          "id": "uuid-of-school-class-here"
        },
        "section": {
          "id": "uuid-of-section-here"
        },
        "admissionDate": "2024-01-01",
        "motherInfo": {
          "name": "Susan Brown",
          "occupation": "Accountant",
          "phone": "+1234567904",
          "email": "susan.brown@example.com"
        },
        "emergencyContact": {
          "name": "Susan Brown",
          "relation": "Mother",
          "phone": "+1234567904"
        },
        "createUserAccount": true
      }
    ],
    "batchReference": "BULK-CREATION-001",
    "continueOnError": true
  }' \
  | jq '.'

# ============================================
# 4. BULK UPLOAD WITH DUPLICATE EMAIL (for error testing)
# ============================================
echo "=== Testing Duplicate Email Error Handling ==="
curl -X POST "${BASE_URL}/students/bulk" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "students": [
      {
        "userRequest": {
          "firstName": "Test",
          "lastName": "Duplicate",
          "email": "alice.johnson@example.com",
          "phone": "+1234567999"
        },
        "dateOfBirth": "2010-01-01",
        "gender": "MALE",
        "schoolClass": {
          "id": "uuid-of-school-class-here"
        },
        "section": {
          "id": "uuid-of-section-here"
        },
        "admissionDate": "2024-01-01",
        "emergencyContact": {
          "name": "Emergency Contact",
          "phone": "+1234567999"
        },
        "createUserAccount": true
      }
    ],
    "batchReference": "DUPLICATE-TEST-001",
    "continueOnError": true
  }' \
  | jq '.'

# ============================================
# 5. BULK UPLOAD WITH INVALID DATA (for validation testing)
# ============================================
echo "=== Testing Invalid Data Validation ==="
curl -X POST "${BASE_URL}/students/bulk" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "students": [
      {
        "userRequest": {
          "firstName": "",
          "lastName": "Invalid",
          "email": "invalid-email-format",
          "phone": "123"
        },
        "dateOfBirth": null,
        "gender": "",
        "schoolClass": {
          "id": "invalid-uuid"
        },
        "section": {
          "id": "invalid-uuid"
        },
        "admissionDate": null,
        "emergencyContact": {
          "phone": ""
        },
        "createUserAccount": true
      }
    ],
    "batchReference": "VALIDATION-TEST-002",
    "continueOnError": true
  }' \
  | jq '.'

# ============================================
# 6. TEST MISSING REQUEST BODY (should return 400, not 500)
# ============================================
echo "=== Testing Missing Request Body (should return 400) ==="
curl -X POST "${BASE_URL}/students/bulk" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  | jq '.'

# ============================================
# 7. TEST MALFORMED JSON (should return 400, not 500)
# ============================================
echo "=== Testing Malformed JSON (should return 400) ==="
curl -X POST "${BASE_URL}/students/bulk" \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "students": [
      {
        "userRequest": {
          "firstName": "Test",
          "lastName": "User"
        },
        "dateOfBirth": "2010-01-01",
        "gender": "MALE",
        "schoolClass": {
          "id": "uuid-of-school-class-here"
        },
        "section": {
          "id": "uuid-of-section-here"
        },
        "admissionDate": "2024-01-01",
        "emergencyContact": {
          "name": "Emergency Contact",
          "phone": "+1234567899"
        },
        "createUserAccount": true
      }
    ],
    "batchReference": "MALFORMED-JSON-TEST",
    "continueOnError": true
  ' \
  | jq '.'

echo "============================================"
echo "All cURL commands completed!"
echo "============================================"
