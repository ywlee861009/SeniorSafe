/**
 * Queue-based mock Supabase client.
 *
 * Tests push expected responses in the order the handler will consume them.
 * Each await on a query builder pops the next response from the queue.
 */

// deno-lint-ignore no-explicit-any
type MockResult = { data: any; error: any; count?: number };

const _queue: MockResult[] = [];

/** Push an expected response onto the queue. */
export function mockResponse(
  // deno-lint-ignore no-explicit-any
  data: any,
  // deno-lint-ignore no-explicit-any
  error: any = null,
  count?: number,
) {
  _queue.push({ data, error, count });
}

/** Clear the response queue. */
export function resetMockResponses() {
  _queue.length = 0;
}

/** Number of unconsumed responses remaining. */
export function pendingResponses(): number {
  return _queue.length;
}

class MockQueryBuilder {
  // Every chainable method returns `this`
  // deno-lint-ignore no-explicit-any
  select(_columns?: string, _opts?: any) { return this; }
  // deno-lint-ignore no-explicit-any
  insert(_data: any) { return this; }
  // deno-lint-ignore no-explicit-any
  update(_data: any) { return this; }
  delete() { return this; }
  // deno-lint-ignore no-explicit-any
  eq(_col: string, _val: any) { return this; }
  // deno-lint-ignore no-explicit-any
  is(_col: string, _val: any) { return this; }
  // deno-lint-ignore no-explicit-any
  not(_col: string, _op: string, _val: any) { return this; }
  // deno-lint-ignore no-explicit-any
  order(_col: string, _opts?: any) { return this; }
  limit(_n: number) { return this; }
  range(_from: number, _to: number) { return this; }
  single() { return this; }
  maybeSingle() { return this; }

  /** Makes the builder awaitable — pops next response from queue. */
  then(
    resolve: (value: MockResult) => void,
    reject?: (reason: Error) => void,
  ) {
    const result = _queue.shift();
    if (!result) {
      const err = new Error(
        "MockQueryBuilder: no more responses in queue. " +
        "Did you forget to call mockResponse()?",
      );
      if (reject) return reject(err);
      throw err;
    }
    return Promise.resolve(result).then(resolve);
  }
}

/** Create a mock Supabase client. */
export function createMockClient() {
  return {
    from: (_table: string) => new MockQueryBuilder(),
  };
}
