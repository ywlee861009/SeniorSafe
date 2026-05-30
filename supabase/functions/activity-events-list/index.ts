import { serve } from "https://deno.land/std@0.208.0/http/server.ts";
import { corsHeaders } from "../_shared/cors.ts";
import { getServiceClient } from "../_shared/supabase.ts";
import { getDeviceFromRequest } from "../_shared/auth.ts";

/**
 * GET activity-events-list?senior_device_id=<uuid>&limit=50&offset=0
 *
 * Returns activity events for a senior device.
 * - Senior: can read own events
 * - Guardian: can read events of paired seniors only
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
    const seniorDeviceId = url.searchParams.get("senior_device_id");
    const limit = Math.min(parseInt(url.searchParams.get("limit") ?? "50", 10), 200);
    const offset = parseInt(url.searchParams.get("offset") ?? "0", 10);

    // Determine which senior to query
    let targetSeniorId: string;

    if (device.role === "senior") {
      // Senior can only read own events
      targetSeniorId = device.id;
    } else if (device.role === "guardian") {
      if (!seniorDeviceId) {
        return new Response(
          JSON.stringify({ error: "senior_device_id is required for guardians" }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } },
        );
      }
      targetSeniorId = seniorDeviceId;

      // Verify active pairing exists
      const supabase = getServiceClient();
      const { data: pairing } = await supabase
        .from("pairings")
        .select("id")
        .eq("senior_device_id", targetSeniorId)
        .eq("guardian_device_id", device.id)
        .eq("active", true)
        .limit(1)
        .single();

      if (!pairing) {
        return new Response(
          JSON.stringify({ error: "No active pairing with this senior" }),
          { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } },
        );
      }
    } else {
      return new Response(
        JSON.stringify({ error: "Invalid device role" }),
        { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    const supabase = getServiceClient();
    const { data: events, error, count } = await supabase
      .from("activity_events")
      .select("id, occurred_at, received_at, source", { count: "exact" })
      .eq("senior_device_id", targetSeniorId)
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
