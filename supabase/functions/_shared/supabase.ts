import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

// deno-lint-ignore no-explicit-any
let _mockClient: any = undefined;
// deno-lint-ignore no-explicit-any
export function __setMockClient(client: any) { _mockClient = client; }
export function __resetMockClient() { _mockClient = undefined; }

export function getServiceClient() {
  if (_mockClient !== undefined) return _mockClient;
  return createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
  );
}

export function getAnonClient() {
  return createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_ANON_KEY")!,
  );
}
