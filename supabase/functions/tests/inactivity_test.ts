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

import { handler as inactivityCheckHandler } from "../inactivity-check/index.ts";
import { handler as inactivityAlertsListHandler } from "../inactivity-alerts-list/index.ts";

// ─── inactivity-check ───────────────────────────────────────────

Deno.test("inactivity-check: no inactive seniors → no alerts", async () => {
  setupMocks();
  Deno.env.delete("CRON_SECRET");

  // query seniors → all active within threshold
  mockResponse([
    {
      id: SENIOR_DEVICE.id,
      display_name: "할머니",
      last_activity_at: new Date().toISOString(), // just now
      inactivity_threshold_days: 2,
    },
  ], null);

  const req = jsonRequest("http://localhost/inactivity-check", {});
  const res = await inactivityCheckHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.checked, 1);
  assertEquals(body.alerts_sent, 0);
  teardownMocks();
});

Deno.test("inactivity-check: sends alert for inactive senior", async () => {
  setupMocks();
  Deno.env.delete("CRON_SECRET");
  Deno.env.set("FIREBASE_SERVER_KEY", "fake-key");

  const threeDaysAgo = new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString();

  // 1. query seniors
  mockResponse([
    {
      id: SENIOR_DEVICE.id,
      display_name: "할머니",
      last_activity_at: threeDaysAgo,
      inactivity_threshold_days: 2,
    },
  ], null);
  // 2. get pairings for senior
  mockResponse([
    { guardian_device_id: GUARDIAN_DEVICE.id },
  ], null);
  // 3. dedup check → no previous alert (not found)
  mockResponse(null, null);
  // 4. get guardian FCM token
  mockResponse({ fcm_token: "guardian-fcm-token" }, null);
  // 5. insert alerts
  mockResponse(null, null);

  // Mock fetch for FCM
  const originalFetch = globalThis.fetch;
  globalThis.fetch = ((_url: string | URL | Request) => {
    return Promise.resolve(new Response(JSON.stringify({ success: 1 }), { status: 200 }));
  }) as typeof fetch;

  try {
    const req = jsonRequest("http://localhost/inactivity-check", {});
    const res = await inactivityCheckHandler(req);
    assertEquals(res.status, 200);

    const body = await parseJson(res);
    assertEquals(body.checked, 1);
    assertEquals(body.alerts_sent, 1);
    assertEquals(body.alerts_deduplicated, 0);
  } finally {
    globalThis.fetch = originalFetch;
    Deno.env.delete("FIREBASE_SERVER_KEY");
    teardownMocks();
  }
});

Deno.test("inactivity-check: deduplicates when last_activity_at unchanged", async () => {
  setupMocks();
  Deno.env.delete("CRON_SECRET");

  const threeDaysAgo = new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString();

  // 1. query seniors
  mockResponse([
    {
      id: SENIOR_DEVICE.id,
      display_name: "할머니",
      last_activity_at: threeDaysAgo,
      inactivity_threshold_days: 2,
    },
  ], null);
  // 2. get pairings
  mockResponse([
    { guardian_device_id: GUARDIAN_DEVICE.id },
  ], null);
  // 3. dedup check → found previous alert with same last_activity_at
  mockResponse({
    id: "prev-alert",
    last_activity_at: threeDaysAgo, // same as current
  }, null);
  // (no more queries — skipped)

  const req = jsonRequest("http://localhost/inactivity-check", {});
  const res = await inactivityCheckHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.alerts_sent, 0);
  assertEquals(body.alerts_deduplicated, 1);
  teardownMocks();
});

Deno.test("inactivity-check: skips guardian without FCM token", async () => {
  setupMocks();
  Deno.env.delete("CRON_SECRET");

  const threeDaysAgo = new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString();

  // 1. query seniors
  mockResponse([
    {
      id: SENIOR_DEVICE.id,
      display_name: "할머니",
      last_activity_at: threeDaysAgo,
      inactivity_threshold_days: 2,
    },
  ], null);
  // 2. get pairings
  mockResponse([
    { guardian_device_id: GUARDIAN_DEVICE.id },
  ], null);
  // 3. dedup check → no previous alert
  mockResponse(null, null);
  // 4. get guardian → no FCM token
  mockResponse({ fcm_token: null }, null);
  // 5. insert alerts (skipped status)
  mockResponse(null, null);

  const req = jsonRequest("http://localhost/inactivity-check", {});
  const res = await inactivityCheckHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.alerts_skipped, 1);
  assertEquals(body.alerts_sent, 0);
  teardownMocks();
});

Deno.test("inactivity-check: rejects wrong cron secret", async () => {
  setupMocks();
  Deno.env.set("CRON_SECRET", "correct-secret");

  const req = new Request("http://localhost/inactivity-check", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer wrong-secret",
    },
    body: JSON.stringify({}),
  });
  const res = await inactivityCheckHandler(req);
  assertEquals(res.status, 401);

  Deno.env.delete("CRON_SECRET");
  teardownMocks();
});

// ─── inactivity-alerts-list ─────────────────────────────────────

Deno.test("inactivity-alerts-list: rejects unauthenticated", async () => {
  setupMocks(null);
  const req = getRequest("http://localhost/inactivity-alerts-list");
  const res = await inactivityAlertsListHandler(req);
  assertEquals(res.status, 401);
  teardownMocks();
});

Deno.test("inactivity-alerts-list: guardian reads own alerts", async () => {
  setupMocks(GUARDIAN_DEVICE);

  mockResponse([
    {
      id: "alert-1",
      senior_device_id: SENIOR_DEVICE.id,
      guardian_device_id: GUARDIAN_DEVICE.id,
      threshold_days: 2,
      last_activity_at: "2026-05-25T10:00:00Z",
      sent_at: "2026-05-27T00:00:00Z",
      status: "sent",
      detail: null,
    },
  ], null, 1);

  const req = getRequest("http://localhost/inactivity-alerts-list");
  const res = await inactivityAlertsListHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.alerts.length, 1);
  assertEquals(body.total, 1);
  teardownMocks();
});

Deno.test("inactivity-alerts-list: guardian filters by senior with pairing check", async () => {
  setupMocks(GUARDIAN_DEVICE);

  // 1. pairing check → found
  mockResponse({ id: "pairing-1" }, null);
  // 2. query alerts
  mockResponse([
    {
      id: "alert-1",
      senior_device_id: SENIOR_DEVICE.id,
      guardian_device_id: GUARDIAN_DEVICE.id,
      threshold_days: 2,
      last_activity_at: "2026-05-25T10:00:00Z",
      sent_at: "2026-05-27T00:00:00Z",
      status: "sent",
      detail: null,
    },
  ], null, 1);

  const req = getRequest(
    `http://localhost/inactivity-alerts-list?senior_device_id=${SENIOR_DEVICE.id}`,
  );
  const res = await inactivityAlertsListHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.alerts.length, 1);
  teardownMocks();
});

Deno.test("inactivity-alerts-list: guardian denied without pairing", async () => {
  setupMocks(GUARDIAN_DEVICE);

  // pairing check → not found
  mockResponse(null, { code: "PGRST116" });

  const req = getRequest(
    `http://localhost/inactivity-alerts-list?senior_device_id=${SENIOR_DEVICE.id}`,
  );
  const res = await inactivityAlertsListHandler(req);
  assertEquals(res.status, 403);
  teardownMocks();
});

Deno.test("inactivity-alerts-list: senior reads own alerts", async () => {
  setupMocks(SENIOR_DEVICE);

  mockResponse([
    {
      id: "alert-1",
      senior_device_id: SENIOR_DEVICE.id,
      guardian_device_id: GUARDIAN_DEVICE.id,
      threshold_days: 2,
      last_activity_at: "2026-05-25T10:00:00Z",
      sent_at: "2026-05-27T00:00:00Z",
      status: "sent",
      detail: null,
    },
  ], null, 1);

  const req = getRequest("http://localhost/inactivity-alerts-list");
  const res = await inactivityAlertsListHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.alerts.length, 1);
  teardownMocks();
});
