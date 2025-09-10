#!/bin/bash

# Test 1: Update global config
echo "Testing global config update..."
curl -v -X POST 'http://localhost:8080/api/config/update' \
  -H 'Content-Type: application/json' \
  -d '{
    "scope": "features",
    "key": "google_pay",
    "value": {
      "enabled": true,
      "merchantId": "GLOBAL_MERCHANT"
    }
  }'

echo -e "\n\nTest 2: Get the updated config..."
curl -v 'http://localhost:8080/api/config/mobile'

echo -e "\n\nTest 3: Try with invalid data..."
curl -v -X POST 'http://localhost:8080/api/config/update' \
  -H 'Content-Type: application/json' \
  -d '{"invalid": "data"}'
