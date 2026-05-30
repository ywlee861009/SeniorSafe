import { serve } from "https://deno.land/std@0.208.0/http/server.ts";
import { corsHeaders } from "../_shared/cors.ts";
import { getServiceClient } from "../_shared/supabase.ts";
import { getDeviceFromRequest } from "../_shared/auth.ts";

export const handler = async (req: Request): Promise<Response> => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    // Authenticate
    const device = await getDeviceFromRequest(req);
    if (!device) {
      return new Response(
        JSON.stringify({ error: "Unauthorized" }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    // Only guardians can claim pairing codes
    if (device.role !== "guardian") {
      return new Response(
        JSON.stringify({ error: "Only guardian devices can connect seniors" }),
        { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    const { code } = await req.json();
    if (!code || code.length !== 6) {
      return new Response(
        JSON.stringify({ error: "6-digit code is required" }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    const supabase = getServiceClient();

    // Look up pairing code
    const { data: pairingCode, error: codeError } = await supabase
      .from("pairing_codes")
      .select("code, senior_device_id, expires_at, consumed_at")
      .eq("code", code)
      .is("consumed_at", null)
      .single();

    if (codeError || !pairingCode) {
      return new Response(
        JSON.stringify({ error: "Invalid pairing code" }),
        { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    // Check expiration
    if (new Date(pairingCode.expires_at) <= new Date()) {
      return new Response(
        JSON.stringify({ error: "Expired pairing code" }),
        { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    // Get senior device info
    const { data: senior } = await supabase
      .from("devices")
      .select("id, display_name")
      .eq("id", pairingCode.senior_device_id)
      .single();

    if (!senior) {
      return new Response(
        JSON.stringify({ error: "Senior device not found" }),
        { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    // Check for existing pairing
    const { data: existingPairing } = await supabase
      .from("pairings")
      .select("id")
      .eq("senior_device_id", senior.id)
      .eq("guardian_device_id", device.id)
      .single();

    let pairingId: string;

    if (existingPairing) {
      // Reactivate existing pairing
      const { error } = await supabase
        .from("pairings")
        .update({
          status: "active",
          active: true,
          disconnected_at: null,
        })
        .eq("id", existingPairing.id);

      if (error) throw error;
      pairingId = existingPairing.id;
    } else {
      // Create new pairing
      const { data: newPairing, error } = await supabase
        .from("pairings")
        .insert({
          senior_device_id: senior.id,
          guardian_device_id: device.id,
          status: "active",
          active: true,
        })
        .select("id")
        .single();

      if (error) throw error;
      pairingId = newPairing!.id;
    }

    // Mark code as consumed
    await supabase
      .from("pairing_codes")
      .update({ consumed_at: new Date().toISOString() })
      .eq("code", code);

    return new Response(
      JSON.stringify({
        pairing_id: pairingId,
        senior_device_id: senior.id,
        senior_display_name: senior.display_name,
        created_at: new Date().toISOString(),
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
