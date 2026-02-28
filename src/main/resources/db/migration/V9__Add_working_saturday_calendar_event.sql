-- V9__Add_working_saturday_calendar_event.sql
-- Add calendar event for Saturday working day

INSERT INTO calendar_events (
    id,
    tenant_id,
    event_date,
    event_type,
    title,
    description,
    is_working_day,
    half_day,
    created_at,
    updated_at,
    created_by,
    updated_by
) VALUES (
    gen_random_uuid(),
    'YOUR_TENANT_ID', -- Replace with actual tenant ID
    '2026-02-28',
    'WORKING_DAY',
    'Working Saturday',
    'Saturday marked as working day for attendance',
    true,
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
);
