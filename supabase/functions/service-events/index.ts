import { serve } from "https://deno.land/std@0.208.0/http/server.ts";
import { corsHeaders } from "../_shared/cors.ts";
import { getServiceClient } from "../_shared/supabase.ts";
import { getDeviceFromRequest } from "../_shared/auth.ts";

/**
 * POST service-events
 *
 * Upload service lifecycle events (started, stopped, heartbeat, error).
 * Any device can upload its own service events.
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

    const { events } = await req.json();

    if (!Array.isArray(events) || events.length === 0) {
      return new Response(
        JSON.stringify({ error: "events array is required" }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    const supabase = getServiceClient();
    const now = new Date().toISOString();

    const rows = events.map((e: { event_type: string; occurred_at: string; detail?: string }) => ({
      device_id: device.id,
      event_type: e.event_type,
      occurred_at: e.occurred_at,
      received_at: now,
      detail: e.detail ?? null,
    }));

    const { error } = await supabase.from("service_events").insert(rows);
    if (error) throw error;

    return new Response(
      JSON.stringify({ accepted: events.length }),
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
