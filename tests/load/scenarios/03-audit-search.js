// Audit log read API baseline. Each VU logs in once and reuses the access token across
// iterations to isolate the cost of the paginated search itself (Argon2 verification ran
// once per VU, not per request).
//
// Why this matters: the audit endpoint will be hit from the admin compliance dashboard
// with a small set of saved filters. The fixed-sort plan over the indexed
// occurred_at/actor/target columns should keep p95 well below the user-perceptible
// threshold; a regression here would point at index drift or at the JPQL `coalesce` form
// inadvertently disabling index usage.

import http from 'k6/http';
import { check, group } from 'k6';
import { BASE_URL } from '../lib/env.js';
import { authHeaders, loginAsAdmin } from '../lib/auth.js';

export const options = {
  scenarios: {
    audit_search: {
      executor: 'constant-vus',
      vus: 5,
      duration: '30s',
      tags: { scenario: 'audit_search' },
    },
  },
  thresholds: {
    'http_req_failed{scenario:audit_search}': ['rate<0.01'],
    'http_req_duration{name:GET /audit-events unfiltered}': ['p(95)<300', 'p(99)<600'],
    'http_req_duration{name:GET /audit-events filtered}': ['p(95)<300', 'p(99)<600'],
  },
};

export function setup() {
  // Login once at the start of the test, share the token with every VU. Avoids burning
  // the Argon2-bound login budget on a request we are not measuring here.
  return { jwt: loginAsAdmin() };
}

export default function auditSearchScenario(data) {
  const headers = authHeaders(data.jwt);

  group('audit log search', () => {
    const unfiltered = http.get(
      `${BASE_URL}/api/v1/audit-events?page=1&size=25`,
      { headers, tags: { name: 'GET /audit-events unfiltered' } },
    );
    check(unfiltered, {
      'unfiltered 200': (r) => r.status === 200,
    });

    const filtered = http.get(
      `${BASE_URL}/api/v1/audit-events?targetType=USER&page=1&size=25`,
      { headers, tags: { name: 'GET /audit-events filtered' } },
    );
    check(filtered, {
      'filtered 200': (r) => r.status === 200,
    });
  });
}
