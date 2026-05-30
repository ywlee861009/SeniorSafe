-- Add service_events table for tracking service lifecycle events
-- (started, stopped, heartbeat, error)

create table public.service_events (
  id uuid primary key default gen_random_uuid(),
  device_id uuid not null references public.devices(id) on delete cascade,
  event_type text not null check (event_type in ('started', 'stopped', 'heartbeat', 'error')),
  occurred_at timestamptz not null,
  received_at timestamptz not null default now(),
  detail text
);

create index idx_service_events_device on public.service_events(device_id);
create index idx_service_events_occurred on public.service_events(occurred_at desc);

-- RLS
alter table public.service_events enable row level security;

-- Device can insert its own service events
create policy "service_events_insert_own" on public.service_events
  for insert with check (device_id = auth.uid());

-- Device can read its own service events;
-- Guardian can read paired senior's service events
create policy "service_events_select" on public.service_events
  for select using (
    device_id = auth.uid()
    or exists (
      select 1 from public.pairings
      where pairings.senior_device_id = service_events.device_id
        and pairings.guardian_device_id = auth.uid()
        and pairings.active = true
    )
  );
