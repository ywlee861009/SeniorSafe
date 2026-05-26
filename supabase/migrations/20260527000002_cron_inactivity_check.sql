-- Enable pg_cron extension (available on Supabase free tier)
create extension if not exists pg_cron;

-- Schedule inactivity check to run daily at 9:00 AM KST (00:00 UTC)
-- This calls the inactivity-check Edge Function
select cron.schedule(
  'inactivity-check-daily',
  '0 0 * * *',  -- Every day at 00:00 UTC (09:00 KST)
  $$
  select
    net.http_post(
      url := current_setting('app.settings.supabase_url') || '/functions/v1/inactivity-check',
      headers := jsonb_build_object(
        'Content-Type', 'application/json',
        'Authorization', 'Bearer ' || current_setting('app.settings.cron_secret')
      ),
      body := '{}'::jsonb
    );
  $$
);
