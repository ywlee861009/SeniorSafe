import {
  assertEquals,
} from "https://deno.land/std@0.208.0/assert/mod.ts";
import {
  setupMocks,
  teardownMocks,
  jsonRequest,
  parseJson,
  SENIOR_DEVICE,
} from "./_helpers/setup.ts";
import { mockResponse } from "./_helpers/mock_client.ts";

// Import handlers (serve is no-op via import map)
import { handler as deviceRegisterHandler } from "../device-register/index.ts";
import { handler as fcmTokenHandler } from "../fcm-token/index.ts";

// ─── device-register ────────────────────────────────────────────

Deno.test("device-register: CORS preflight returns ok", async () => {
  setupMocks();
  const req = new Request("http://localhost/device-register", { method: "OPTIONS" });
  const res = await deviceRegisterHandler(req);
  assertEquals(res.status, 200);
  teardownMocks();
});

Deno.test("device-register: rejects missing fields", async () => {
  setupMocks();
  const req = jsonRequest("http://localhost/device-register", { install_id: "abc" });
  const res = await deviceRegisterHandler(req);
  assertEquals(res.status, 400);
  const body = await parseJson(res);
  assertEquals(body.error, "install_id, role, display_name are required");
  teardownMocks();
});

Deno.test("device-register: rejects invalid role", async () => {
  setupMocks();
  const req = jsonRequest("http://localhost/device-register", {
    install_id: "abc",
    role: "admin",
    display_name: "test",
  });
  const res = await deviceRegisterHandler(req);
  assertEquals(res.status, 400);
  const body = await parseJson(res);
  assertEquals(body.error, "role must be 'senior' or 'guardian'");
  teardownMocks();
});

Deno.test("device-register: creates new device", async () => {
  setupMocks();

  // 1. select existing device → not found
  mockResponse(null, { code: "PGRST116", message: "not found" });
  // 2. insert new device → returns id
  mockResponse({ id: "new-device-uuid" }, null);
  // 3. update token_hash
  mockResponse(null, null);

  const req = jsonRequest("http://localhost/device-register", {
    install_id: "install-123",
    role: "senior",
    display_name: "할머니",
  });
  const res = await deviceRegisterHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.device_id, "new-device-uuid");
  assertEquals(body.role, "senior");
  assertEquals(body.display_name, "할머니");
  assertEquals(body.device_access_token, "mock-token-for-testing");
  teardownMocks();
});

Deno.test("device-register: updates existing device", async () => {
  setupMocks();

  // 1. select existing device → found
  mockResponse({ id: "existing-uuid" }, null);
  // 2. update device fields
  mockResponse(null, null);
  // 3. update token_hash
  mockResponse(null, null);

  const req = jsonRequest("http://localhost/device-register", {
    install_id: "install-123",
    role: "guardian",
    display_name: "보호자",
    fcm_token: "fcm-abc",
  });
  const res = await deviceRegisterHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.device_id, "existing-uuid");
  assertEquals(body.role, "guardian");
  teardownMocks();
});

// ─── fcm-token ──────────────────────────────────────────────────

Deno.test("fcm-token: rejects unauthenticated request", async () => {
  setupMocks(null);
  const req = jsonRequest("http://localhost/fcm-token", { fcm_token: "abc" }, "PUT");
  const res = await fcmTokenHandler(req);
  assertEquals(res.status, 401);
  teardownMocks();
});

Deno.test("fcm-token: rejects missing fcm_token", async () => {
  setupMocks(SENIOR_DEVICE);
  const req = jsonRequest("http://localhost/fcm-token", {}, "PUT");
  const res = await fcmTokenHandler(req);
  assertEquals(res.status, 400);
  teardownMocks();
});

Deno.test("fcm-token: updates token successfully", async () => {
  setupMocks(SENIOR_DEVICE);

  // update query
  mockResponse(null, null);

  const req = jsonRequest("http://localhost/fcm-token", { fcm_token: "new-fcm-token" }, "PUT");
  const res = await fcmTokenHandler(req);
  assertEquals(res.status, 200);

  const body = await parseJson(res);
  assertEquals(body.message, "FCM token updated");
  teardownMocks();
});
