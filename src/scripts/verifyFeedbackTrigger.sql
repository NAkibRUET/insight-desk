-- Check if the trigger exists
SELECT trigger_name, action_timing, event_manipulation, action_statement
FROM information_schema.triggers
WHERE event_object_table = 'feedback';

SELECT
    pg_get_functiondef(p.oid)
FROM
    pg_proc p
WHERE
    p.proname = 'feedback_tsv_trigger';