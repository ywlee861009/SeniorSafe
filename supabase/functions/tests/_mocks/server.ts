// No-op serve for testing — prevents Edge Functions from starting an HTTP server
// deno-lint-ignore no-explicit-any
export function serve(_handler: any) {
  // intentionally empty
}
