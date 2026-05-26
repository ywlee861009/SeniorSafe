-- SeniorSafe Initial Schema for Supabase
-- Devices, Pairings, PairingCodes, ActivityEvents, InactivityAlerts

-- ============================================================
-- Extensions
-- ============================================================
create extension if not exists "pgcrypto";

-- ============================================================
-- Tables
-- ============================================================

-- Devices table
create table public.devices (
  id uuid primary key default gen_random_uuid(),
  install_id_hash text not null unique,
  role text not null check (role in ('senior', 'guardian')),
  display_name text not null,
  fcm_token text,
  token_hash text,
  created_at timestamptz not null default now(),
  last_seen_at timestamptz not null default now(),
  last_activity_at timestamptz,
  inactivity_threshold_days int not null default 2
);

create index idx_devices_install_id_hash on public.devices(install_id_hash);
create index idx_devices_role on public.devices(role);

-- Pairing codes table
create table public.pairing_codes (
  code text primary key,
  senior_device_id uuid not null references public.devices(id) on delete cascade,
  expires_at timestamptz not null,
  consumed_at timestamptz,
  created_at timestamptz not null default now()
);

create index idx_pairing_codes_senior on public.pairing_codes(senior_device_id);

-- Pairings table
create table public.pairings (
  id uuid primary key default gen_random_uuid(),
  senior_device_id uuid not null references public.devices(id) on delete cascade,
  guardian_device_id uuid not null references public.devices(id) on delete cascade,
  status text not null default 'active' check (status in ('pending', 'active', 'disconnected')),
  active boolean not null default true,
  created_at timestamptz not null default now(),
  disconnected_at timestamptz,
  unique (senior_device_id, guardian_device_id)
);

create index idx_pairings_senior on public.pairings(senior_device_id);
create index idx_pairings_guardian on public.pairings(guardian_device_id);

-- Activity events table (for future use)
create table public.activity_events (
  id uuid primary key default gen_random_uuid(),
  senior_device_id uuid not null references public.devices(id) on delete cascade,
  occurred_at timestamptz not null,
  received_at timestamptz not null default now(),
  source text not null check (source in ('user_present', 'power_connected', 'power_disconnected'))
);

create index idx_activity_events_senior on public.activity_events(senior_device_id);
create index idx_activity_events_occurred on public.activity_events(occurred_at desc);

-- Inactivity alerts table (for future use)
create table public.inactivity_alerts (
  id uuid primary key default gen_random_uuid(),
  senior_device_id uuid not null references public.devices(id) on delete cascade,
  guardian_device_id uuid not null references public.devices(id) on delete cascade,
  threshold_days int not null,
  last_activity_at timestamptz,
  sent_at timestamptz not null default now(),
  status text not null default 'sent' check (status in ('sent', 'skipped', 'failed')),
  detail text
);

-- ============================================================
-- Row Level Security (RLS)
-- ============================================================

alter table public.devices enable row level security;
alter table public.pairing_codes enable row level security;
alter table public.pairings enable row level security;
alter table public.activity_events enable row level security;
alter table public.inactivity_alerts enable row level security;

-- Devices: device can only read/update itself
create policy "devices_select_own" on public.devices
  for select using (id = auth.uid());

create policy "devices_update_own" on public.devices
  for update using (id = auth.uid());

-- Pairing codes: senior can manage own codes
create policy "pairing_codes_select" on public.pairing_codes
  for select using (senior_device_id = auth.uid());

create policy "pairing_codes_insert" on public.pairing_codes
  for insert with check (senior_device_id = auth.uid());

create policy "pairing_codes_delete" on public.pairing_codes
  for delete using (senior_device_id = auth.uid());

-- Pairings: both sides can read their own pairings
create policy "pairings_select" on public.pairings
  for select using (
    senior_device_id = auth.uid() or guardian_device_id = auth.uid()
  );

-- Activity events: senior can insert own events, guardians can read paired seniors
create policy "activity_events_insert" on public.activity_events
  for insert with check (senior_device_id = auth.uid());

create policy "activity_events_select" on public.activity_events
  for select using (
    senior_device_id = auth.uid()
    or exists (
      select 1 from public.pairings
      where pairings.senior_device_id = activity_events.senior_device_id
        and pairings.guardian_device_id = auth.uid()
        and pairings.active = true
    )
  );

-- Inactivity alerts: guardian can read own alerts
create policy "inactivity_alerts_select" on public.inactivity_alerts
  for select using (guardian_device_id = auth.uid());

-- ============================================================
-- Service role bypass (for Edge Functions)
-- ============================================================
-- Edge Functions use service_role key which bypasses RLS by default.
-- No additional policies needed for server-side operations.
