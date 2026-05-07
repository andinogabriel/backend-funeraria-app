// Login throughput baseline. Each VU performs an isolated login per iteration so we measure
// the cost of the full auth pipeline (Argon2 verification, refresh-token issuance, audit
// path on activation is not exercised here because the bootstrap admin is already enabled).
//
// Why this matters: login latency is the most user-visible synchronous operation in the
// app, and Argon2 verification dominates its cost. A regression on this number is the
// canary for a hashing-parameter bump or a HikariCP saturation issue under contention.

import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL, JSON_HEADERS, ADMIN_EMAIL, ADMIN_PASSWORD } from '../lib/env.js';

export const options = {
  scenarios: {
    login_steady: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '20s', target: 5 },
        { duration: '40s', target: 5 },
        { duration: '10s', target: 0 },
      ],
      gracefulRampDown: '5s',
      tags: { scenario: 'login_steady' },
    },
  },
  thresholds: {
    'http_req_failed{name:POST /users/login}': ['rate<0.01'],
    'http_req_duration{name:POST /users/login}': ['p(95)<800', 'p(99)<1500'],
  },
};

export default function loginScenario() {
  const payload = JSON.stringify({
    email: ADMIN_EMAIL,
    password: ADMIN_PASSWORD,
    deviceInfo: { deviceId: `k6-${__VU}-${__ITER}`, deviceType: 'k6-load' },
  });
  const res = http.post(`${BASE_URL}/api/v1/users/login`, payload, {
    headers: JSON_HEADERS,
    tags: { name: 'POST /users/login' },
  });
  check(res, {
    'status is 200': (r) => r.status === 200,
    'has access token': (r) => r.json('authorization') !== undefined,
  });
}
