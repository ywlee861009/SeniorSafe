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

import { handler as pairingCodesHandler } from "../pairing-codes/index.ts";
import { handler as pairingClaimHandler } from "../pairing-claim/index.ts";
import { handler as pairingDisconnectHandler } from "../pairing-disconnect/index.ts";
import { handler as pairingsListHandler } from "../pairings-list/index.ts";

// ─── pairing-codes ──────────────────────────────────────────────

Deno.test("pairing-codes: rejects unauthenticated", async () => {
  setupMocks(null);
  const req = jsonRequest("http://localhost/pairing-codes", {});
  const res = await pairingCodesHandler(req);
  assertEquals(res.status, 401);
  teardownMocks();
});

Deno.test("pairing-codes: rejects guardian role", async () => {
  setupMocks(GUARDIAN_DEVICE);
  const req = jsonRequest("http://localhost/pairing-codes", {});
  const res = await pairingCodesHandler(req);
  assertEquals(res.status, 403);
  teardownMocks();
});

Deno.test("pairing-codes: generates code for senior", async () => {
  setupMocks(SENIOR_DEVICE);

  // 1. delete existing unconsumed codes
  mockResponse(null, null);
  // 2. check code collision → not found (code is unique)
  mockResponse(null, { code: "PGRST116" });
  // 3. insert pairing code
  mockResponse(null, null);

  const req = jsonRequest("http://localhost/pairing-codes", {});
  const res = await pairingCodesHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(typeof body.code, "string");
  assertEquals(body.code.length, 6);
  assertEquals(body.expires_in_seconds, 600);
  teardownMocks();
});

// ─── pairing-claim ──────────────────────────────────────────────

Deno.test("pairing-claim: rejects senior role", async () => {
  setupMocks(SENIOR_DEVICE);
  const req = jsonRequest("http://localhost/pairing-claim", { code: "123456" });
  const res = await pairingClaimHandler(req);
  assertEquals(res.status, 403);
  teardownMocks();
});

Deno.test("pairing-claim: rejects invalid code length", async () => {
  setupMocks(GUARDIAN_DEVICE);
  const req = jsonRequest("http://localhost/pairing-claim", { code: "12" });
  const res = await pairingClaimHandler(req);
  assertEquals(res.status, 400);
  teardownMocks();
});

Deno.test("pairing-claim: rejects non-existent code", async () => {
  setupMocks(GUARDIAN_DEVICE);

  // lookup pairing code → not found
  mockResponse(null, { code: "PGRST116" });

  const req = jsonRequest("http://localhost/pairing-claim", { code: "999999" });
  const res = await pairingClaimHandler(req);
  assertEquals(res.status, 404);
  teardownMocks();
});

Deno.test("pairing-claim: rejects expired code", async () => {
  setupMocks(GUARDIAN_DEVICE);

  // lookup pairing code → found but expired
  mockResponse({
    code: "123456",
    senior_device_id: SENIOR_DEVICE.id,
    expires_at: new Date(Date.now() - 60000).toISOString(), // expired 1 min ago
    consumed_at: null,
  }, null);

  const req = jsonRequest("http://localhost/pairing-claim", { code: "123456" });
  const res = await pairingClaimHandler(req);
  assertEquals(res.status, 404);
  teardownMocks();
});

Deno.test("pairing-claim: creates new pairing", async () => {
  setupMocks(GUARDIAN_DEVICE);

  // 1. lookup pairing code → valid
  mockResponse({
    code: "123456",
    senior_device_id: SENIOR_DEVICE.id,
    expires_at: new Date(Date.now() + 300000).toISOString(),
    consumed_at: null,
  }, null);
  // 2. get senior device info
  mockResponse({ id: SENIOR_DEVICE.id, display_name: "할머니" }, null);
  // 3. check existing pairing → none
  mockResponse(null, { code: "PGRST116" });
  // 4. create new pairing
  mockResponse({ id: "pairing-uuid-001" }, null);
  // 5. mark code as consumed
  mockResponse(null, null);

  const req = jsonRequest("http://localhost/pairing-claim", { code: "123456" });
  const res = await pairingClaimHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.pairing_id, "pairing-uuid-001");
  assertEquals(body.senior_device_id, SENIOR_DEVICE.id);
  assertEquals(body.senior_display_name, "할머니");
  teardownMocks();
});

Deno.test("pairing-claim: reactivates existing pairing", async () => {
  setupMocks(GUARDIAN_DEVICE);

  // 1. lookup code
  mockResponse({
    code: "654321",
    senior_device_id: SENIOR_DEVICE.id,
    expires_at: new Date(Date.now() + 300000).toISOString(),
    consumed_at: null,
  }, null);
  // 2. get senior device
  mockResponse({ id: SENIOR_DEVICE.id, display_name: "할머니" }, null);
  // 3. existing pairing found
  mockResponse({ id: "pairing-uuid-existing" }, null);
  // 4. update pairing (reactivate)
  mockResponse(null, null);
  // 5. mark code consumed
  mockResponse(null, null);

  const req = jsonRequest("http://localhost/pairing-claim", { code: "654321" });
  const res = await pairingClaimHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.pairing_id, "pairing-uuid-existing");
  teardownMocks();
});

// ─── pairing-disconnect ─────────────────────────────────────────

Deno.test("pairing-disconnect: rejects unauthenticated", async () => {
  setupMocks(null);
  const req = jsonRequest("http://localhost/pairing-disconnect", { pairing_id: "abc" });
  const res = await pairingDisconnectHandler(req);
  assertEquals(res.status, 401);
  teardownMocks();
});

Deno.test("pairing-disconnect: rejects non-member device", async () => {
  setupMocks(GUARDIAN_DEVICE);

  // fetch pairing → found but device is not a member
  mockResponse({
    id: "pairing-uuid",
    senior_device_id: "other-senior",
    guardian_device_id: "other-guardian",
  }, null);

  const req = jsonRequest("http://localhost/pairing-disconnect", { pairing_id: "pairing-uuid" });
  const res = await pairingDisconnectHandler(req);
  assertEquals(res.status, 404);
  teardownMocks();
});

Deno.test("pairing-disconnect: disconnects successfully (guardian)", async () => {
  setupMocks(GUARDIAN_DEVICE);

  // 1. fetch pairing
  mockResponse({
    id: "pairing-uuid",
    senior_device_id: SENIOR_DEVICE.id,
    guardian_device_id: GUARDIAN_DEVICE.id,
  }, null);
  // 2. update pairing
  mockResponse(null, null);

  const req = jsonRequest("http://localhost/pairing-disconnect", { pairing_id: "pairing-uuid" });
  const res = await pairingDisconnectHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.active, false);
  teardownMocks();
});

// ─── pairings-list ──────────────────────────────────────────────

Deno.test("pairings-list: rejects unauthenticated", async () => {
  setupMocks(null);
  const req = getRequest("http://localhost/pairings-list");
  const res = await pairingsListHandler(req);
  assertEquals(res.status, 401);
  teardownMocks();
});

Deno.test("pairings-list: returns guardian view with senior info", async () => {
  setupMocks(GUARDIAN_DEVICE);

  mockResponse([
    {
      id: "pairing-1",
      active: true,
      created_at: "2026-05-01T00:00:00Z",
      senior_device_id: SENIOR_DEVICE.id,
      devices: {
        display_name: "할머니",
        last_seen_at: "2026-05-28T10:00:00Z",
        last_activity_at: "2026-05-28T09:00:00Z",
        inactivity_threshold_days: 2,
      },
    },
  ], null);

  const req = getRequest("http://localhost/pairings-list");
  const res = await pairingsListHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.pairings.length, 1);
  assertEquals(body.pairings[0].senior_display_name, "할머니");
  assertEquals(body.pairings[0].inactivity_threshold_days, 2);
  teardownMocks();
});

Deno.test("pairings-list: returns senior view with guardian info", async () => {
  setupMocks(SENIOR_DEVICE);

  mockResponse([
    {
      id: "pairing-1",
      active: true,
      created_at: "2026-05-01T00:00:00Z",
      guardian_device_id: GUARDIAN_DEVICE.id,
      devices: {
        display_name: "보호자",
        last_seen_at: "2026-05-28T10:00:00Z",
      },
    },
  ], null);

  const req = getRequest("http://localhost/pairings-list");
  const res = await pairingsListHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.pairings.length, 1);
  assertEquals(body.pairings[0].guardian_display_name, "보호자");
  teardownMocks();
});

Deno.test("pairings-list: returns empty list when no pairings", async () => {
  setupMocks(GUARDIAN_DEVICE);
  mockResponse([], null);

  const req = getRequest("http://localhost/pairings-list");
  const res = await pairingsListHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.pairings.length, 0);
  teardownMocks();
});
