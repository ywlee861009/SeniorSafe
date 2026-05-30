/**
 * Common test setup/teardown helpers.
 */
import { __setMockClient, __resetMockClient } from "../../_shared/supabase.ts";
import { __setMockDevice, __resetMockDevice } from "../../_shared/auth.ts";
import type { DeviceInfo } from "../../_shared/auth.ts";
import { __setMockToken, __resetMockToken } from "../../_shared/jwt.ts";
import { createMockClient, resetMockResponses } from "./mock_client.ts";

/** Install mock client + optional device, return the client for response setup. */
export function setupMocks(device?: DeviceInfo | null) {
  const client = createMockClient();
  __setMockClient(client);
  if (device !== undefined) {
    __setMockDevice(device);
  }
  __setMockToken("mock-token-for-testing");
  return client;
}

/** Reset all mocks — call in afterEach or test cleanup. */
export function teardownMocks() {
  __resetMockClient();
  __resetMockDevice();
  __resetMockToken();
  resetMockResponses();
}

/** Shorthand to build a JSON POST request. */
// deno-lint-ignore no-explicit-any
export function jsonRequest(url: string, body: any, method = "POST"): Request {
  return new Request(url, {
    method,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}

/** Shorthand to build a GET request. */
export function getRequest(url: string): Request {
  return new Request(url, { method: "GET" });
}

/** Parse JSON body from Response. */
// deno-lint-ignore no-explicit-any
export async function parseJson(res: Response): Promise<any> {
  return await res.json();
}

/** Common test device fixtures. */
export const SENIOR_DEVICE: DeviceInfo = {
  id: "senior-uuid-001",
  role: "senior",
  display_name: "할머니",
};

export const GUARDIAN_DEVICE: DeviceInfo = {
  id: "guardian-uuid-001",
  role: "guardian",
  display_name: "보호자",
};
