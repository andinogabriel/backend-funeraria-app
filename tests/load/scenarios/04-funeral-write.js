// Write-heavy baseline: end-to-end funeral creation. Each iteration POSTs a unique funeral
// (unique receipt number + DNI) so we measure the cost of the full transactional path:
// FuneralCommandUseCase -> deceased registration -> draft factory -> persistence ->
// audit-event REQUIRES_NEW write.
//
// Why this matters: this is the only synchronous *write* hot path the app exposes. A
// regression here would point at FK lookups, audit overhead, or transactional boundaries
// — exactly the kind of change that the read-side scenarios (01..03) cannot detect.
//
// Setup:
// - One admin login per test run, shared across VUs (auth cost is not measured here).
// - One plan created at setup() with an empty itemsPlan; the plan id is shared so every
//   funeral references the same plan and the metric isolates the funeral path itself.
// - Catalog FKs reuse the seed values from V2__seed_reference_data.sql:
//     gender id 2 (Masculino), relationship id 1 (Padre),
//     death_cause id 2 (Muerte clínica), receipt_type id 1 (Recibo de caja de ingreso).

import http from 'k6/http';
import { check, fail } from 'k6';
import { BASE_URL, JSON_HEADERS } from '../lib/env.js';
import { authHeaders, loginAsAdmin } from '../lib/auth.js';

export const options = {
  scenarios: {
    funeral_write: {
      executor: 'constant-vus',
      vus: 3,
      duration: '40s',
      tags: { scenario: 'funeral_write' },
    },
  },
  thresholds: {
    'http_req_failed{name:POST /funerals}': ['rate<0.02'],
    'http_req_duration{name:POST /funerals}': ['p(95)<1200', 'p(99)<2000'],
  },
};

export function setup() {
  const jwt = loginAsAdmin();
  const headers = authHeaders(jwt);

  const planPayload = JSON.stringify({
    name: `k6-load-plan-${Date.now()}`,
    description: 'Plan created by the k6 funeral-write baseline',
    profitPercentage: 25.0,
    itemsPlan: [],
  });
  const planRes = http.post(`${BASE_URL}/api/v1/plans`, planPayload, {
    headers,
    tags: { name: 'POST /plans (setup)' },
  });
  if (planRes.status !== 201) {
    fail(`plan setup failed: status=${planRes.status} body=${planRes.body}`);
  }
  const planId = planRes.json('id');

  return { jwt, planId };
}

export default function funeralWriteScenario(data) {
  const headers = authHeaders(data.jwt);
  // Globally unique suffix so receiptNumber / DNI never collide across VUs and iterations.
  const suffix = `${__VU}-${__ITER}-${Date.now()}`;
  // DNI must fit in a Java Integer; offset from a non-real range to avoid colliding with
  // any production-shaped data that might be loaded into the same database later.
  const dni = 90000000 + (__VU * 1000 + __ITER) % 9999999;

  const payload = JSON.stringify({
    funeralDate: nowPlusOneDayIso(),
    receiptNumber: `K6-${suffix}`,
    receiptSeries: 'K6',
    tax: 21.0,
    receiptType: { id: 1, name: 'Recibo de caja de ingreso' },
    deceased: {
      firstName: 'LoadTest',
      lastName: `Deceased-${suffix}`,
      dni: dni,
      birthDate: '1970-01-01',
      deathDate: '2026-05-01',
      gender: { id: 2, name: 'Masculino' },
      deceasedRelationship: { id: 1, name: 'Padre' },
      deathCause: { id: 2, name: 'Muerte clínica' },
    },
    plan: {
      id: data.planId,
      name: 'k6-load-plan',
      profitPercentage: 25.0,
      itemsPlan: [],
    },
  });

  const res = http.post(`${BASE_URL}/api/v1/funerals`, payload, {
    headers,
    tags: { name: 'POST /funerals' },
  });
  check(res, {
    'funeral 201': (r) => r.status === 201,
    'has id': (r) => r.json('id') !== undefined,
  });
}

/**
 * Returns an ISO-8601 LocalDateTime string (no zone) one day in the future. The
 * funeral endpoint validates `@FutureOrPresent` on `funeralDate`, so we always submit a
 * value that satisfies the constraint regardless of clock skew between k6 and the app.
 */
function nowPlusOneDayIso() {
  const tomorrow = new Date(Date.now() + 24 * 60 * 60 * 1000);
  return tomorrow.toISOString().slice(0, 19); // strip the milliseconds + 'Z' suffix
}
