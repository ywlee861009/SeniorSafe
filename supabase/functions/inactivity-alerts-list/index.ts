import { serve } from "https://deno.land/std@0.208.0/http/server.ts";
import { corsHeaders } from "../_shared/cors.ts";
import { getServiceClient } from "../_shared/supabase.ts";
import { getDeviceFromRequest } from "../_shared/auth.ts";

/**
 * GET inactivity-alerts-list?senior_device_id=<uuid>&limit=50&offset=0
 *
 * Returns inactivity alert history.
 * - Guardian: alerts where they are the guardian (optionally filtered by senior)
 * - Senior: alerts about themselves
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

    const supabase = getServiceClient();

    let query = supabase
      .from("inactivity_alerts")
      .select("id, senior_device_id, guardian_device_id, threshold_days, last_activity_at, sent_at, status, detail", { count: "exact" })
      .order("sent_at", { ascending: false })
      .range(offset, offset + limit - 1);

    if (device.role === "guardian") {
      query = query.eq("guardian_device_id", device.id);
      if (seniorDeviceId) {
        // Verify active pairing
        const { data: pairing } = await supabase
          .from("pairings")
          .select("id")
          .eq("senior_device_id", seniorDeviceId)
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
        query = query.eq("senior_device_id", seniorDeviceId);
      }
    } else if (device.role === "senior") {
      query = query.eq("senior_device_id", device.id);
    } else {
      return new Response(
        JSON.stringify({ error: "Invalid device role" }),
        { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    const { data: alerts, error, count } = await query;
    if (error) throw error;

    return new Response(
      JSON.stringify({
        alerts: alerts ?? [],
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
