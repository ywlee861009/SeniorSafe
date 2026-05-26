import { serve } from "https://deno.land/std@0.208.0/http/server.ts";
import { corsHeaders } from "../_shared/cors.ts";
import { getServiceClient } from "../_shared/supabase.ts";
import { getDeviceFromRequest } from "../_shared/auth.ts";

const PAIRING_CODE_EXPIRE_MINUTES = 10;

function generateCode(): string {
  const digits = "0123456789";
  let code = "";
  const arr = new Uint8Array(6);
  crypto.getRandomValues(arr);
  for (const byte of arr) {
    code += digits[byte % 10];
  }
  return code;
}

serve(async (req) => {
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

    // Only seniors can create pairing codes
    if (device.role !== "senior") {
      return new Response(
        JSON.stringify({ error: "Only senior devices can create pairing codes" }),
        { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    const supabase = getServiceClient();

    // Delete existing unconsumed codes for this senior
    await supabase
      .from("pairing_codes")
      .delete()
      .eq("senior_device_id", device.id)
      .is("consumed_at", null);

    // Generate unique 6-digit code
    let code: string = "";
    for (let attempt = 0; attempt < 10; attempt++) {
      const candidate = generateCode();
      const { data } = await supabase
        .from("pairing_codes")
        .select("code")
        .eq("code", candidate)
        .single();

      if (!data) {
        code = candidate;
        break;
      }
    }

    if (!code) {
      return new Response(
        JSON.stringify({ error: "Failed to generate pairing code" }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    // Create pairing code
    const expiresAt = new Date(
      Date.now() + PAIRING_CODE_EXPIRE_MINUTES * 60 * 1000,
    ).toISOString();

    const { error } = await supabase.from("pairing_codes").insert({
      code,
      senior_device_id: device.id,
      expires_at: expiresAt,
    });

    if (error) throw error;

    return new Response(
      JSON.stringify({
        code,
        expires_at: expiresAt,
        expires_in_seconds: PAIRING_CODE_EXPIRE_MINUTES * 60,
      }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } },
    );
  } catch (err) {
    return new Response(
      JSON.stringify({ error: (err as Error).message }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } },
    );
  }
});
