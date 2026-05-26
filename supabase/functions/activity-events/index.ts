import { serve } from "https://deno.land/std@0.208.0/http/server.ts";
import { corsHeaders } from "../_shared/cors.ts";
import { getServiceClient } from "../_shared/supabase.ts";
import { getDeviceFromRequest } from "../_shared/auth.ts";

serve(async (req) => {
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

    if (device.role !== "senior") {
      return new Response(
        JSON.stringify({ error: "Only senior devices can upload activity events" }),
        { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } },
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

    // Insert events
    const rows = events.map((e: { occurred_at: string; source: string }) => ({
      senior_device_id: device.id,
      occurred_at: e.occurred_at,
      received_at: now,
      source: e.source,
    }));

    const { error } = await supabase.from("activity_events").insert(rows);
    if (error) throw error;

    // Update device last_activity_at to the most recent event
    const latestOccurred = events.reduce(
      (latest: string, e: { occurred_at: string }) =>
        e.occurred_at > latest ? e.occurred_at : latest,
      events[0].occurred_at,
    );

    await supabase
      .from("devices")
      .update({ last_activity_at: latestOccurred })
      .eq("id", device.id);

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
});
