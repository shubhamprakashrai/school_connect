#!/bin/bash
# ===================================================================
# School Management System - Database Backup Script
# ===================================================================
# Usage: ./scripts/backup.sh [daily|weekly|manual]
# Requires: pg_dump, gzip
#
# Environment variables (set in .env or export):
#   DB_HOST       - PostgreSQL host (default: localhost)
#   DB_PORT       - PostgreSQL port (default: 5432)
#   DB_NAME       - Database name (default: school_mgmt_db)
#   DB_USER       - Database user (default: school_admin)
#   BACKUP_DIR    - Backup directory (default: ./backups)
#   BACKUP_RETAIN - Days to retain backups (default: 30)
# ===================================================================

set -euo pipefail

# Configuration
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-school_mgmt_db}"
DB_USER="${DB_USER:-school_admin}"
BACKUP_DIR="${BACKUP_DIR:-./backups}"
BACKUP_RETAIN="${BACKUP_RETAIN:-30}"
BACKUP_TYPE="${1:-manual}"

# Timestamp
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}_${BACKUP_TYPE}_${TIMESTAMP}.sql.gz"

# Ensure backup directory exists
mkdir -p "${BACKUP_DIR}"

echo "=== School Management DB Backup ==="
echo "Type: ${BACKUP_TYPE}"
echo "Database: ${DB_NAME}@${DB_HOST}:${DB_PORT}"
echo "Output: ${BACKUP_FILE}"
echo "===================================="

# Perform backup
echo "[$(date)] Starting backup..."
pg_dump -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" \
    --format=plain \
    --no-owner \
    --no-privileges \
    --verbose 2>/dev/null | gzip > "${BACKUP_FILE}"

# Verify backup
if [ -f "${BACKUP_FILE}" ] && [ -s "${BACKUP_FILE}" ]; then
    BACKUP_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)
    echo "[$(date)] Backup completed: ${BACKUP_FILE} (${BACKUP_SIZE})"
else
    echo "[$(date)] ERROR: Backup failed or file is empty"
    exit 1
fi

# Clean old backups
echo "[$(date)] Cleaning backups older than ${BACKUP_RETAIN} days..."
find "${BACKUP_DIR}" -name "${DB_NAME}_*.sql.gz" -mtime "+${BACKUP_RETAIN}" -delete 2>/dev/null
REMAINING=$(find "${BACKUP_DIR}" -name "${DB_NAME}_*.sql.gz" | wc -l | tr -d ' ')
echo "[$(date)] Remaining backups: ${REMAINING}"

echo "[$(date)] Backup complete."
