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

    // Extract pairing_id from URL or body
    const url = new URL(req.url);
    const pathParts = url.pathname.split("/");
    let pairingId = pathParts[pathParts.length - 1];

    // If no ID in path, try request body
    if (!pairingId || pairingId === "pairing-disconnect") {
      const body = await req.json();
      pairingId = body.pairing_id;
    }

    if (!pairingId) {
      return new Response(
        JSON.stringify({ error: "pairing_id is required" }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    const supabase = getServiceClient();

    // Fetch pairing
    const { data: pairing, error: fetchError } = await supabase
      .from("pairings")
      .select("id, senior_device_id, guardian_device_id")
      .eq("id", pairingId)
      .single();

    if (fetchError || !pairing) {
      return new Response(
        JSON.stringify({ error: "Pairing not found" }),
        { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    // Verify device is part of this pairing
    if (
      device.id !== pairing.senior_device_id &&
      device.id !== pairing.guardian_device_id
    ) {
      return new Response(
        JSON.stringify({ error: "Pairing not found" }),
        { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    // Disconnect
    const { error: updateError } = await supabase
      .from("pairings")
      .update({
        active: false,
        status: "disconnected",
        disconnected_at: new Date().toISOString(),
      })
      .eq("id", pairingId);

    if (updateError) throw updateError;

    return new Response(
      JSON.stringify({
        pairing_id: pairingId,
        active: false,
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
