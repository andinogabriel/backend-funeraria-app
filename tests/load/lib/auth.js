// Authentication helpers for k6 scenarios. The login flow returns a JwtDto whose
// `authorization` field already carries the prefix expected on the Authorization header
// (e.g. `Bearer <token>`), so callers can copy it verbatim into request headers.

import http from 'k6/http';
import { check, fail } from 'k6';
import { ADMIN_EMAIL, ADMIN_PASSWORD, BASE_URL, JSON_HEADERS } from './env.js';

/**
 * Logs in as the bootstrap admin and returns the parsed JwtDto. Fails the iteration loud
 * and clear if the response is not 200, so a misconfigured BASE_URL or wrong credentials
 * surface immediately instead of silently degrading the throughput numbers.
 */
export function loginAsAdmin() {
  const payload = JSON.stringify({
    email: ADMIN_EMAIL,
    password: ADMIN_PASSWORD,
    deviceInfo: { deviceId: `k6-${__VU}-${__ITER}`, deviceType: 'k6-load' },
  });

  const res = http.post(`${BASE_URL}/api/v1/users/login`, payload, {
    headers: JSON_HEADERS,
    tags: { name: 'POST /users/login' },
  });

  const ok = check(res, { 'login 200': (r) => r.status === 200 });
  if (!ok) {
    fail(`login failed: status=${res.status} body=${res.body}`);
  }
  return res.json();
}

/**
 * Returns request headers ready for an authenticated call. The supplied JwtDto must have
 * been obtained via {@link loginAsAdmin}; the `authorization` field already includes the
 * scheme prefix.
 */
export function authHeaders(jwt) {
  return {
    ...JSON_HEADERS,
    Authorization: jwt.authorization,
  };
}
