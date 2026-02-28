# Subject API - cURL Commands
# ================================================

# Base URL (adjust as needed)
BASE_URL="http://localhost:8080/api/v1"

# Authentication token (replace with actual JWT token)
AUTH_TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJ0ZW5hbnRJZCI6IlNNMDAwMDEiLCJzdHVkZW50TG9naW5SZXF1aXJlZCI6ZmFsc2UsInVzZXJuYW1lIjoiU00wMDAwMTAwMDAxIiwic3ViIjoiU00wMDAwMTAwMDAxIiwiaWF0IjoxNzcyMDE0NDk1LCJleHAiOjE3NzIxMDA4OTV9.I-0bJf03JXM1lvs_GBkfkVehQfD7oU7cU7ccCNZFRDY"

# ================================================
# 1. CREATE SUBJECT
# ================================================
echo "=== CREATE SUBJECT ==="
curl -X POST "${BASE_URL}/subjects" \
  -H "Authorization: ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "MATH101",
    "name": "Mathematics",
    "description": "Mathematics for Grade 10"
  }' | jq '.'

echo -e "\n"

# Create Physics subject
curl -X POST "${BASE_URL}/subjects" \
  -H "Authorization: ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "PHY101", 
    "name": "Physics",
    "description": "Physics for Grade 10"
  }' | jq '.'

echo -e "\n"

# Create Chemistry subject  
curl -X POST "${BASE_URL}/subjects" \
  -H "Authorization: ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "CHEM101",
    "name": "Chemistry", 
    "description": "Chemistry for Grade 10"
  }' | jq '.'

echo -e "\n\n"

# ================================================
# 2. GET SUBJECT BY ID
# ================================================
echo "=== GET SUBJECT BY ID ==="
# Replace SUBJECT_ID with actual UUID from create response
SUBJECT_ID="550e8400-e29b-41d4-a716-446655440000"

curl -X GET "${BASE_URL}/subjects/${SUBJECT_ID}" \
  -H "Authorization: ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" | jq '.'

echo -e "\n\n"

# ================================================
# 3. GET SUBJECT BY CODE
# ================================================
echo "=== GET SUBJECT BY CODE ==="
curl -X GET "${BASE_URL}/subjects/code/MATH101" \
  -H "Authorization: ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" | jq '.'

echo -e "\n\n"

# ================================================
# 4. GET ALL SUBJECTS
# ================================================
echo "=== GET ALL SUBJECTS ==="
curl -X GET "${BASE_URL}/subjects" \
  -H "Authorization: ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" | jq '.'

echo -e "\n\n"

# ================================================
# 5. GET PAGINATED SUBJECTS
# ================================================
echo "=== GET PAGINATED SUBJECTS ==="
curl -X GET "${BASE_URL}/subjects/paginated?page=0&size=5&sortBy=name&sortDir=asc" \
  -H "Authorization: ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" | jq '.'

echo -e "\n"

# Get second page
curl -X GET "${BASE_URL}/subjects/paginated?page=1&size=5&sortBy=name&sortDir=desc" \
  -H "Authorization: ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" | jq '.'

echo -e "\n\n"

# ================================================
# 6. SEARCH SUBJECTS
# ================================================
echo "=== SEARCH SUBJECTS ==="
# Search by name
curl -X GET "${BASE_URL}/subjects/search?searchTerm=Math&page=0&size=10&sortBy=name&sortDir=asc" \
  -H "Authorization: ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" | jq '.'

echo -e "\n"

# Search by code
curl -X GET "${BASE_URL}/subjects/search?searchTerm=PHY&page=0&size=10&sortBy=code&sortDir=asc" \
  -H "Authorization: ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" | jq '.'

echo -e "\n\n"

# ================================================
# 7. UPDATE SUBJECT
# ================================================
echo "=== UPDATE SUBJECT ==="
# Replace SUBJECT_ID with actual UUID
curl -X PUT "${BASE_URL}/subjects/${SUBJECT_ID}" \
  -H "Authorization: ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "MATH101",
    "name": "Advanced Mathematics",
    "description": "Advanced Mathematics for Grade 10 with additional topics"
  }' | jq '.'

echo -e "\n\n"

# ================================================
# 8. DELETE SUBJECT
# ================================================
echo "=== DELETE SUBJECT ==="
# Replace SUBJECT_ID with actual UUID
curl -X DELETE "${BASE_URL}/subjects/${SUBJECT_ID}" \
  -H "Authorization: ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" | jq '.'

echo -e "\n\n"

# ================================================
# 9. ERROR HANDLING EXAMPLES
# ================================================
echo "=== ERROR HANDLING EXAMPLES ==="

# Create subject with duplicate code (should return 409)
echo "Creating duplicate subject..."
curl -X POST "${BASE_URL}/subjects" \
  -H "Authorization: ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "MATH101",
    "name": "Another Math",
    "description": "Duplicate code test"
  }' | jq '.'

echo -e "\n"

# Get non-existent subject (should return 404)
echo "Getting non-existent subject..."
curl -X GET "${BASE_URL}/subjects/550e8400-e29b-41d4-a716-44665544999" \
  -H "Authorization: ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" | jq '.'

echo -e "\n"

# Create subject with invalid data (should return 400)
echo "Creating subject with invalid data..."
curl -X POST "${BASE_URL}/subjects" \
  -H "Authorization: ${AUTH_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "",
    "name": "",
    "description": "Invalid subject with empty code and name"
  }' | jq '.'

echo -e "\n\n"

# ================================================
# 10. BATCH OPERATIONS EXAMPLE
# ================================================
echo "=== BATCH OPERATIONS EXAMPLE ==="

# Create multiple subjects in sequence
echo "Creating multiple subjects..."
subjects_data=(
  '{"code": "BIO101", "name": "Biology", "description": "Biology for Grade 10"}'
  '{"code": "ENG101", "name": "English", "description": "English Literature for Grade 10"}'
  '{"code": "HIST101", "name": "History", "description": "World History for Grade 10"}'
)

for subject in "${subjects_data[@]}"; do
  echo "Creating: $subject"
  curl -X POST "${BASE_URL}/subjects" \
    -H "Authorization: ${AUTH_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "$subject" | jq '.data | {id, code, name}'
  echo -e "\n"
done

echo "=== SUBJECT API TESTING COMPLETE ==="
