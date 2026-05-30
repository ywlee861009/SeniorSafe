import { serve } from "https://deno.land/std@0.208.0/http/server.ts";
import { corsHeaders } from "../_shared/cors.ts";
import { getServiceClient } from "../_shared/supabase.ts";
import { getDeviceFromRequest } from "../_shared/auth.ts";

/**
 * GET service-events-list?device_id=<uuid>&limit=50&offset=0
 *
 * Returns service events for a device.
 * - Own device: can read own events
 * - Guardian: can read paired senior's events
 */
export const handler = async (req: Request): Promise<Response> => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const device = await getDeviceFromRequest(req);
    if (!device) {
      return new Response(
        JSON.stringify({ error: "Unauthorized" }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    const url = new URL(req.url);
    const targetDeviceId = url.searchParams.get("device_id") ?? device.id;
    const limit = Math.min(parseInt(url.searchParams.get("limit") ?? "50", 10), 200);
    const offset = parseInt(url.searchParams.get("offset") ?? "0", 10);

    const supabase = getServiceClient();

    // If querying another device, verify active pairing (guardian reading senior's data)
    if (targetDeviceId !== device.id) {
      if (device.role !== "guardian") {
        return new Response(
          JSON.stringify({ error: "Only guardians can read other devices' service events" }),
          { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } },
        );
      }

      const { data: pairing } = await supabase
        .from("pairings")
        .select("id")
        .eq("senior_device_id", targetDeviceId)
        .eq("guardian_device_id", device.id)
        .eq("active", true)
        .limit(1)
        .single();

      if (!pairing) {
        return new Response(
          JSON.stringify({ error: "No active pairing with this device" }),
          { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } },
        );
      }
    }

    const { data: events, error, count } = await supabase
      .from("service_events")
      .select("id, event_type, occurred_at, received_at, detail", { count: "exact" })
      .eq("device_id", targetDeviceId)
      .order("occurred_at", { ascending: false })
      .range(offset, offset + limit - 1);

    if (error) throw error;

    return new Response(
      JSON.stringify({
        events: events ?? [],
        total: count ?? 0,
        limit,
        offset,
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
