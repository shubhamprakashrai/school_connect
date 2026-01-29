#!/bin/bash
# ===================================================================
# School Management System - Database Restore Script
# ===================================================================
# Usage: ./scripts/restore.sh <backup_file.sql.gz>
# ===================================================================

set -euo pipefail

if [ $# -eq 0 ]; then
    echo "Usage: $0 <backup_file.sql.gz>"
    echo ""
    echo "Available backups:"
    ls -lh ./backups/*.sql.gz 2>/dev/null || echo "  No backups found in ./backups/"
    exit 1
fi

BACKUP_FILE="$1"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-school_mgmt_db}"
DB_USER="${DB_USER:-school_admin}"

if [ ! -f "${BACKUP_FILE}" ]; then
    echo "ERROR: Backup file not found: ${BACKUP_FILE}"
    exit 1
fi

echo "=== School Management DB Restore ==="
echo "Source: ${BACKUP_FILE}"
echo "Target: ${DB_NAME}@${DB_HOST}:${DB_PORT}"
echo "====================================="
echo ""
echo "WARNING: This will overwrite the current database!"
read -p "Continue? (yes/no): " CONFIRM

if [ "${CONFIRM}" != "yes" ]; then
    echo "Restore cancelled."
    exit 0
fi

echo "[$(date)] Restoring database..."
gunzip -c "${BACKUP_FILE}" | psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" --quiet

echo "[$(date)] Restore completed successfully."
