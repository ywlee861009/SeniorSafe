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

import { handler as serviceEventsHandler } from "../service-events/index.ts";
import { handler as serviceEventsListHandler } from "../service-events-list/index.ts";

// ─── service-events (POST) ──────────────────────────────────────

Deno.test("service-events: rejects unauthenticated", async () => {
  setupMocks(null);
  const req = jsonRequest("http://localhost/service-events", { events: [] });
  const res = await serviceEventsHandler(req);
  assertEquals(res.status, 401);
  teardownMocks();
});

Deno.test("service-events: rejects empty events", async () => {
  setupMocks(SENIOR_DEVICE);
  const req = jsonRequest("http://localhost/service-events", { events: [] });
  const res = await serviceEventsHandler(req);
  assertEquals(res.status, 400);
  teardownMocks();
});

Deno.test("service-events: accepts valid events from any device", async () => {
  setupMocks(SENIOR_DEVICE);
  mockResponse(null, null); // insert

  const req = jsonRequest("http://localhost/service-events", {
    events: [
      { event_type: "started", occurred_at: "2026-05-28T10:00:00Z" },
      { event_type: "heartbeat", occurred_at: "2026-05-28T10:05:00Z" },
      { event_type: "error", occurred_at: "2026-05-28T10:06:00Z", detail: "sensor failure" },
    ],
  });
  const res = await serviceEventsHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.accepted, 3);
  teardownMocks();
});

Deno.test("service-events: guardian can also upload own events", async () => {
  setupMocks(GUARDIAN_DEVICE);
  mockResponse(null, null);

  const req = jsonRequest("http://localhost/service-events", {
    events: [
      { event_type: "started", occurred_at: "2026-05-28T10:00:00Z" },
    ],
  });
  const res = await serviceEventsHandler(req);
  assertEquals(res.status, 200);
  teardownMocks();
});

// ─── service-events-list (GET) ──────────────────────────────────

Deno.test("service-events-list: rejects unauthenticated", async () => {
  setupMocks(null);
  const req = getRequest("http://localhost/service-events-list");
  const res = await serviceEventsListHandler(req);
  assertEquals(res.status, 401);
  teardownMocks();
});

Deno.test("service-events-list: device reads own events", async () => {
  setupMocks(SENIOR_DEVICE);

  mockResponse([
    { id: "se-1", event_type: "started", occurred_at: "2026-05-28T10:00:00Z", received_at: "2026-05-28T10:01:00Z", detail: null },
  ], null, 1);

  const req = getRequest("http://localhost/service-events-list");
  const res = await serviceEventsListHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.events.length, 1);
  assertEquals(body.total, 1);
  teardownMocks();
});

Deno.test("service-events-list: guardian reads paired senior events", async () => {
  setupMocks(GUARDIAN_DEVICE);

  // 1. pairing check → found
  mockResponse({ id: "pairing-1" }, null);
  // 2. query events
  mockResponse([
    { id: "se-1", event_type: "heartbeat", occurred_at: "2026-05-28T10:00:00Z", received_at: "2026-05-28T10:01:00Z", detail: null },
  ], null, 1);

  const req = getRequest(
    `http://localhost/service-events-list?device_id=${SENIOR_DEVICE.id}`,
  );
  const res = await serviceEventsListHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.events.length, 1);
  teardownMocks();
});

Deno.test("service-events-list: guardian denied without pairing", async () => {
  setupMocks(GUARDIAN_DEVICE);

  // pairing check → not found
  mockResponse(null, { code: "PGRST116" });

  const req = getRequest(
    `http://localhost/service-events-list?device_id=${SENIOR_DEVICE.id}`,
  );
  const res = await serviceEventsListHandler(req);
  assertEquals(res.status, 403);
  teardownMocks();
});

Deno.test("service-events-list: senior cannot read other device events", async () => {
  setupMocks(SENIOR_DEVICE);

  const req = getRequest(
    `http://localhost/service-events-list?device_id=other-device-id`,
  );
  const res = await serviceEventsListHandler(req);
  assertEquals(res.status, 403);
  teardownMocks();
});
