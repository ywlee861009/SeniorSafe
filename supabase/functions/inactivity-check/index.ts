import { serve } from "https://deno.land/std@0.208.0/http/server.ts";
import { corsHeaders } from "../_shared/cors.ts";
import { getServiceClient } from "../_shared/supabase.ts";

/**
 * Inactivity Check Batch Function
 *
 * Triggered by pg_cron or manual invocation.
 * Finds seniors whose last_activity_at exceeds their inactivity_threshold_days,
 * then sends FCM push to their connected guardians.
 *
 * FCM delivery (see sendFcmNotification):
 *   - Primary: FCM HTTP v1 API via FIREBASE_SERVICE_ACCOUNT
 *     (service account JSON string; OAuth2 / RS256).
 *   - Fallback: legacy HTTP API via FIREBASE_SERVER_KEY.
 *     NOTE: Google shut down the legacy FCM API in 2024 — this path is
 *     effectively dead and kept only for backward compatibility / tests.
 *     Configure FIREBASE_SERVICE_ACCOUNT for real delivery.
 */
export const handler = async (req: Request): Promise<Response> => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    // Optional: verify internal secret for cron invocations
    const authHeader = req.headers.get("authorization");
    const cronSecret = Deno.env.get("CRON_SECRET");
    if (cronSecret && authHeader !== `Bearer ${cronSecret}`) {
      return new Response(
        JSON.stringify({ error: "Unauthorized" }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    const supabase = getServiceClient();

    // Find inactive seniors
    const { data: seniors, error: seniorError } = await supabase
      .from("devices")
      .select("id, display_name, last_activity_at, inactivity_threshold_days")
      .eq("role", "senior")
      .not("last_activity_at", "is", null);

    if (seniorError) throw seniorError;

    const now = new Date();
    const alerts: Array<{
      senior_device_id: string;
      guardian_device_id: string;
      threshold_days: number;
      last_activity_at: string;
      status: string;
      detail: string | null;
    }> = [];

    let alertsDeduplicated = 0;

    for (const senior of seniors || []) {
      const lastActivity = new Date(senior.last_activity_at);
      const daysSince =
        (now.getTime() - lastActivity.getTime()) / (1000 * 60 * 60 * 24);

      if (daysSince < senior.inactivity_threshold_days) continue;

      // Get active guardians for this senior
      const { data: pairings } = await supabase
        .from("pairings")
        .select("guardian_device_id")
        .eq("senior_device_id", senior.id)
        .eq("active", true);

      if (!pairings || pairings.length === 0) continue;

      for (const pairing of pairings) {
        // Deduplication: skip if we already sent an alert for the same
        // last_activity_at (no new activity since last alert)
        const { data: lastAlert } = await supabase
          .from("inactivity_alerts")
          .select("id, last_activity_at")
          .eq("senior_device_id", senior.id)
          .eq("guardian_device_id", pairing.guardian_device_id)
          .eq("status", "sent")
          .order("sent_at", { ascending: false })
          .limit(1)
          .maybeSingle();

        if (
          lastAlert &&
          lastAlert.last_activity_at === senior.last_activity_at
        ) {
          alertsDeduplicated++;
          continue;
        }

        // Get guardian's FCM token
        const { data: guardian } = await supabase
          .from("devices")
          .select("fcm_token")
          .eq("id", pairing.guardian_device_id)
          .single();

        if (!guardian?.fcm_token) {
          alerts.push({
            senior_device_id: senior.id,
            guardian_device_id: pairing.guardian_device_id,
            threshold_days: senior.inactivity_threshold_days,
            last_activity_at: senior.last_activity_at,
            status: "skipped",
            detail: "No FCM token",
          });
          continue;
        }

        // Send FCM push notification
        const fcmResult = await sendFcmNotification(
          guardian.fcm_token,
          senior.display_name,
          Math.floor(daysSince),
        );

        alerts.push({
          senior_device_id: senior.id,
          guardian_device_id: pairing.guardian_device_id,
          threshold_days: senior.inactivity_threshold_days,
          last_activity_at: senior.last_activity_at,
          status: fcmResult ? "sent" : "failed",
          detail: fcmResult ? null : "FCM send failed",
        });
      }
    }

    // Record alerts
    if (alerts.length > 0) {
      await supabase.from("inactivity_alerts").insert(alerts);
    }

    return new Response(
      JSON.stringify({
        checked: seniors?.length ?? 0,
        alerts_sent: alerts.filter((a) => a.status === "sent").length,
        alerts_skipped: alerts.filter((a) => a.status === "skipped").length,
        alerts_failed: alerts.filter((a) => a.status === "failed").length,
        alerts_deduplicated: alertsDeduplicated,
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

async function getGoogleAccessToken(
  clientEmail: string,
  privateKey: string,
): Promise<string> {
  const { create } = await import("https://deno.land/x/djwt@v3.0.2/mod.ts");

  const pemHeader = "-----BEGIN PRIVATE KEY-----";
  const pemFooter = "-----END PRIVATE KEY-----";
  
  let pemContents = privateKey;
  if (privateKey.includes(pemHeader)) {
    pemContents = privateKey.substring(
      privateKey.indexOf(pemHeader) + pemHeader.length,
      privateKey.indexOf(pemFooter),
    );
  }
  
  const cleanPem = pemContents.replace(/\s/g, "");
  const binaryDer = Uint8Array.from(atob(cleanPem), (c) => c.charCodeAt(0));

  const key = await crypto.subtle.importKey(
    "pkcs8",
    binaryDer,
    {
      name: "RSASSA-PKCS1-v1_5",
      hash: "SHA-256",
    },
    false,
    ["sign"],
  );

  const now = Math.floor(Date.now() / 1000);
  const payload = {
    iss: clientEmail,
    scope: "https://www.googleapis.com/auth/firebase.messaging",
    aud: "https://oauth2.googleapis.com/token",
    exp: now + 3600,
    iat: now,
  };

  const jwt = await create({ alg: "RS256", typ: "JWT" }, payload, key);

  const response = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion: jwt,
    }),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Failed to exchange Google OAuth2 token: ${errorText}`);
  }

  const data = await response.json();
  return data.access_token;
}

async function sendFcmNotification(
  fcmToken: string,
  seniorName: string,
  days: number,
): Promise<boolean> {
  const serviceAccountEnv = Deno.env.get("FIREBASE_SERVICE_ACCOUNT");
  const serverKey = Deno.env.get("FIREBASE_SERVER_KEY");

  // 1. Try FCM HTTP v1 (OAuth 2.0)
  if (serviceAccountEnv) {
    try {
      const serviceAccount = JSON.parse(serviceAccountEnv);
      const { client_email, private_key, project_id } = serviceAccount;

      if (client_email && private_key && project_id) {
        const accessToken = await getGoogleAccessToken(client_email, private_key);

        const response = await fetch(
          `https://fcm.googleapis.com/v1/projects/${project_id}/messages:send`,
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${accessToken}`,
            },
            body: JSON.stringify({
              message: {
                token: fcmToken,
                notification: {
                  title: "안부 확인",
                  body: `${seniorName}님의 휴대폰 사용 기록이 ${days}일째 확인되지 않습니다.`,
                },
                data: {
                  type: "inactivity_alert",
                  senior_name: seniorName,
                  days_inactive: String(days),
                },
              },
            }),
          },
        );

        return response.ok;
      }
    } catch (err) {
      console.error("FCM v1 failed, falling back to legacy:", err);
    }
  }

  // 2. Legacy FCM Fallback
  if (!serverKey) return false;

  try {
    const response = await fetch("https://fcm.googleapis.com/fcm/send", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `key=${serverKey}`,
      },
      body: JSON.stringify({
        to: fcmToken,
        notification: {
          title: "안부 확인",
          body: `${seniorName}님의 휴대폰 사용 기록이 ${days}일째 확인되지 않습니다.`,
        },
        data: {
          type: "inactivity_alert",
          senior_name: seniorName,
          days_inactive: String(days),
        },
      }),
    });
    return response.ok;
  } catch (err) {
    console.error("Legacy FCM failed:", err);
    return false;
  }
}
