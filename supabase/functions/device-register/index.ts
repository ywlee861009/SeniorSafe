import { serve } from "https://deno.land/std@0.208.0/http/server.ts";
import { corsHeaders } from "../_shared/cors.ts";
import { getServiceClient } from "../_shared/supabase.ts";
import { createDeviceToken } from "../_shared/jwt.ts";

function hashIdentifier(value: string): Promise<string> {
  const encoder = new TextEncoder();
  return crypto.subtle.digest("SHA-256", encoder.encode(value)).then((buf) =>
    Array.from(new Uint8Array(buf))
      .map((b) => b.toString(16).padStart(2, "0"))
      .join("")
  );
}

export const handler = async (req: Request): Promise<Response> => {
  // CORS preflight
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const { install_id, role, display_name, fcm_token } = await req.json();

    // Validation
    if (!install_id || !role || !display_name) {
      return new Response(
        JSON.stringify({ error: "install_id, role, display_name are required" }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }
    if (!["senior", "guardian"].includes(role)) {
      return new Response(
        JSON.stringify({ error: "role must be 'senior' or 'guardian'" }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    const supabase = getServiceClient();
    const installIdHash = await hashIdentifier(install_id);

    // Check if device already exists
    const { data: existing } = await supabase
      .from("devices")
      .select("id")
      .eq("install_id_hash", installIdHash)
      .single();

    let deviceId: string;

    if (existing) {
      // Update existing device
      deviceId = existing.id;
      await supabase
        .from("devices")
        .update({
          role,
          display_name,
          fcm_token: fcm_token || null,
          last_seen_at: new Date().toISOString(),
        })
        .eq("id", deviceId);
    } else {
      // Create new device
      const { data: newDevice, error } = await supabase
        .from("devices")
        .insert({
          install_id_hash: installIdHash,
          role,
          display_name,
          fcm_token: fcm_token || null,
          last_seen_at: new Date().toISOString(),
        })
        .select("id")
        .single();

      if (error) throw error;
      deviceId = newDevice.id;
    }

    // Generate device access token (custom JWT)
    const token = await createDeviceToken(deviceId);

    // Store token hash
    const tokenHash = await hashIdentifier(token);
    await supabase
      .from("devices")
      .update({ token_hash: tokenHash })
      .eq("id", deviceId);

    return new Response(
      JSON.stringify({
        device_id: deviceId,
        role,
        display_name,
        device_access_token: token,
      }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } },
    );
  } catch (err) {
    return new Response(
      JSON.stringify({ error: (err as Error).message }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } },
    );
  }
};

serve(handler);
