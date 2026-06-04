import { create, getNumericDate } from "https://deno.land/x/djwt@v3.0.2/mod.ts";

const DEVICE_TOKEN_EXPIRE_DAYS = 365;

let _mockToken: string | undefined = undefined;
export function __setMockToken(token: string) { _mockToken = token; }
export function __resetMockToken() { _mockToken = undefined; }

/**
 * Create a custom JWT for device authentication.
 * Signed with DEVICE_JWT_SECRET (SUPABASE_ prefix is reserved and cannot be set
 * as a function secret). Edge Functions verify this token themselves via
 * verifyDeviceToken and access the DB with the service_role key, so the secret
 * only needs to be consistent between sign and verify.
 */
export async function createDeviceToken(deviceId: string): Promise<string> {
  if (_mockToken !== undefined) return _mockToken;
  const secret = Deno.env.get("DEVICE_JWT_SECRET")!;
  const key = await crypto.subtle.importKey(
    "raw",
    new TextEncoder().encode(secret),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"],
  );

  const now = Math.floor(Date.now() / 1000);
  const payload = {
    sub: deviceId,
    role: "authenticated",
    iss: "supabase",
    iat: now,
    exp: getNumericDate(60 * 60 * 24 * DEVICE_TOKEN_EXPIRE_DAYS),
    typ: "device",
  };

  return await create({ alg: "HS256", typ: "JWT" }, payload, key);
}

/**
 * Verify device token and extract device_id.
 * Returns null if token is invalid.
 */
export async function verifyDeviceToken(
  token: string,
): Promise<string | null> {
  try {
    const { verify } = await import("https://deno.land/x/djwt@v3.0.2/mod.ts");
    const secret = Deno.env.get("DEVICE_JWT_SECRET")!;
    const key = await crypto.subtle.importKey(
      "raw",
      new TextEncoder().encode(secret),
      { name: "HMAC", hash: "SHA-256" },
      false,
      ["verify"],
    );
    const payload = await verify(token, key);
    if (payload.typ !== "device") return null;
    return payload.sub as string;
  } catch {
    return null;
  }
}
