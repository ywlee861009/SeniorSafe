import {
  assertEquals,
} from "https://deno.land/std@0.208.0/assert/mod.ts";
import {
  setupMocks,
  teardownMocks,
  jsonRequest,
  getRequest,
  parseJson,
  SENIOR_DEVICE,
  GUARDIAN_DEVICE,
} from "./_helpers/setup.ts";
import { mockResponse } from "./_helpers/mock_client.ts";

import { handler as activityEventsHandler } from "../activity-events/index.ts";
import { handler as activityEventsListHandler } from "../activity-events-list/index.ts";

// ─── activity-events (POST) ─────────────────────────────────────

Deno.test("activity-events: rejects unauthenticated", async () => {
  setupMocks(null);
  const req = jsonRequest("http://localhost/activity-events", { events: [] });
  const res = await activityEventsHandler(req);
  assertEquals(res.status, 401);
  teardownMocks();
});

Deno.test("activity-events: rejects guardian role", async () => {
  setupMocks(GUARDIAN_DEVICE);
  const req = jsonRequest("http://localhost/activity-events", { events: [{ occurred_at: "2026-05-28T10:00:00Z", source: "user_present" }] });
  const res = await activityEventsHandler(req);
  assertEquals(res.status, 403);
  teardownMocks();
});

Deno.test("activity-events: rejects empty events array", async () => {
  setupMocks(SENIOR_DEVICE);
  const req = jsonRequest("http://localhost/activity-events", { events: [] });
  const res = await activityEventsHandler(req);
  assertEquals(res.status, 400);
  teardownMocks();
});

Deno.test("activity-events: accepts valid events", async () => {
  setupMocks(SENIOR_DEVICE);

  // 1. insert events
  mockResponse(null, null);
  // 2. update device last_activity_at
  mockResponse(null, null);

  const events = [
    { occurred_at: "2026-05-28T08:00:00Z", source: "user_present" },
    { occurred_at: "2026-05-28T10:00:00Z", source: "power_connected" },
  ];

  const req = jsonRequest("http://localhost/activity-events", { events });
  const res = await activityEventsHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.accepted, 2);
  teardownMocks();
});

// ─── activity-events-list (GET) ─────────────────────────────────

Deno.test("activity-events-list: rejects unauthenticated", async () => {
  setupMocks(null);
  const req = getRequest("http://localhost/activity-events-list");
  const res = await activityEventsListHandler(req);
  assertEquals(res.status, 401);
  teardownMocks();
});

Deno.test("activity-events-list: senior reads own events", async () => {
  setupMocks(SENIOR_DEVICE);

  const events = [
    { id: "evt-1", occurred_at: "2026-05-28T10:00:00Z", received_at: "2026-05-28T10:01:00Z", source: "user_present" },
    { id: "evt-2", occurred_at: "2026-05-28T08:00:00Z", received_at: "2026-05-28T08:01:00Z", source: "power_connected" },
  ];
  mockResponse(events, null, 2);

  const req = getRequest("http://localhost/activity-events-list");
  const res = await activityEventsListHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.events.length, 2);
  assertEquals(body.total, 2);
  teardownMocks();
});

Deno.test("activity-events-list: guardian requires senior_device_id", async () => {
  setupMocks(GUARDIAN_DEVICE);

  const req = getRequest("http://localhost/activity-events-list");
  const res = await activityEventsListHandler(req);
  assertEquals(res.status, 400);

  const body = await parseJson(res);
  assertEquals(body.error, "senior_device_id is required for guardians");
  teardownMocks();
});

Deno.test("activity-events-list: guardian denied without active pairing", async () => {
  setupMocks(GUARDIAN_DEVICE);

  // pairing check → not found
  mockResponse(null, { code: "PGRST116" });

  const req = getRequest(
    `http://localhost/activity-events-list?senior_device_id=${SENIOR_DEVICE.id}`,
  );
  const res = await activityEventsListHandler(req);
  assertEquals(res.status, 403);
  teardownMocks();
});

Deno.test("activity-events-list: guardian reads paired senior events", async () => {
  setupMocks(GUARDIAN_DEVICE);

  // 1. pairing check → found
  mockResponse({ id: "pairing-1" }, null);
  // 2. query events
  const events = [
    { id: "evt-1", occurred_at: "2026-05-28T10:00:00Z", received_at: "2026-05-28T10:01:00Z", source: "user_present" },
  ];
  mockResponse(events, null, 1);

  const req = getRequest(
    `http://localhost/activity-events-list?senior_device_id=${SENIOR_DEVICE.id}&limit=10`,
  );
  const res = await activityEventsListHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.events.length, 1);
  assertEquals(body.limit, 10);
  assertEquals(body.offset, 0);
  teardownMocks();
});
