import { serve } from "https://deno.land/std@0.208.0/http/server.ts";
import { corsHeaders } from "../_shared/cors.ts";
import { getServiceClient } from "../_shared/supabase.ts";
import { getDeviceFromRequest } from "../_shared/auth.ts";

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

    const supabase = getServiceClient();

    if (device.role === "guardian") {
      // Guardian sees their connected seniors
      const { data: pairings, error } = await supabase
        .from("pairings")
        .select(`
          id,
          active,
          created_at,
          senior_device_id,
          devices!pairings_senior_device_id_fkey (
            id,
            display_name,
            last_seen_at,
            last_activity_at,
            inactivity_threshold_days
          )
        `)
        .eq("guardian_device_id", device.id)
        .eq("active", true)
        .order("created_at", { ascending: false });

      if (error) throw error;

      const items = (pairings || []).map((p: any) => ({
        pairing_id: p.id,
        active: p.active,
        senior_device_id: p.senior_device_id,
        senior_display_name: p.devices?.display_name ?? null,
        last_seen_at: p.devices?.last_seen_at ?? null,
        last_activity_at: p.devices?.last_activity_at ?? null,
        inactivity_threshold_days: p.devices?.inactivity_threshold_days ?? 2,
      }));

      return new Response(
        JSON.stringify({ pairings: items }),
        { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    } else {
      // Senior sees their connected guardians
      const { data: pairings, error } = await supabase
        .from("pairings")
        .select(`
          id,
          active,
          created_at,
          guardian_device_id,
          devices!pairings_guardian_device_id_fkey (
            id,
            display_name,
            last_seen_at
          )
        `)
        .eq("senior_device_id", device.id)
        .eq("active", true)
        .order("created_at", { ascending: false });

      if (error) throw error;

      const items = (pairings || []).map((p: any) => ({
        pairing_id: p.id,
        active: p.active,
        guardian_device_id: p.guardian_device_id,
        guardian_display_name: p.devices?.display_name ?? null,
      }));

      return new Response(
        JSON.stringify({ pairings: items }),
        { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }
  } catch (err) {
    return new Response(
      JSON.stringify({ error: (err as Error).message }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } },
    );
  }
};

serve(handler);
