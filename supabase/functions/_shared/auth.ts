import { getServiceClient } from "./supabase.ts";
import { verifyDeviceToken } from "./jwt.ts";

export interface DeviceInfo {
  id: string;
  role: string;
  display_name: string;
}

/**
 * Extract and verify device from Authorization header.
 * Returns device info or null if unauthorized.
 */
export async function getDeviceFromRequest(
  req: Request,
): Promise<DeviceInfo | null> {
  const authHeader = req.headers.get("authorization");
  if (!authHeader?.startsWith("Bearer ")) return null;

  const token = authHeader.slice(7);
  const deviceId = await verifyDeviceToken(token);
  if (!deviceId) return null;

  const supabase = getServiceClient();
  const { data, error } = await supabase
    .from("devices")
    .select("id, role, display_name")
    .eq("id", deviceId)
    .single();

  if (error || !data) return null;

  // Update last_seen_at
  await supabase
    .from("devices")
    .update({ last_seen_at: new Date().toISOString() })
    .eq("id", deviceId);

  return data as DeviceInfo;
}
