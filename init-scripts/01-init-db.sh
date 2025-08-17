#!/bin/bash
set -e

# Create main database if it doesn't exist
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    SELECT 'CREATE DATABASE school_mgmt_db'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'school_mgmt_db')\gexec
EOSQL

echo "Database initialization completed successfully"
