// Read-heavy baseline against the Caffeine-backed catalog endpoints. After the first hit
// per VU these reads should be served from the in-memory cache, so we expect very low p95
// and zero database round-trips except for the warm-up iteration.
//
// Why this matters: catalog reads are called from every screen of the admin UI on page
// load. A regression here means the cache layer broke (eviction, key collision, profile
// configuration drift) and would multiply the read load on PostgreSQL.

import http from 'k6/http';
import { check, group } from 'k6';
import { BASE_URL, JSON_HEADERS } from '../lib/env.js';

export const options = {
  scenarios: {
    catalog_reads: {
      executor: 'constant-vus',
      vus: 10,
      duration: '30s',
      tags: { scenario: 'catalog_reads' },
    },
  },
  thresholds: {
    'http_req_failed{scenario:catalog_reads}': ['rate<0.005'],
    'http_req_duration{name:GET /provinces}': ['p(95)<150', 'p(99)<300'],
    'http_req_duration{name:GET /cities}': ['p(95)<200', 'p(99)<400'],
  },
};

export default function catalogsScenario() {
  group('cached catalog reads', () => {
    const provinces = http.get(`${BASE_URL}/api/v1/provinces`, {
      headers: JSON_HEADERS,
      tags: { name: 'GET /provinces' },
    });
    check(provinces, {
      'provinces 200': (r) => r.status === 200,
      'provinces non-empty': (r) => Array.isArray(r.json()) && r.json().length > 0,
    });

    // Cities are fetched per-province to mirror how the admin UI populates dropdowns.
    // Province id 1 is seeded by V1__init_schema.sql (24 provinces inserted in order).
    const cities = http.get(`${BASE_URL}/api/v1/cities?province_id=1`, {
      headers: JSON_HEADERS,
      tags: { name: 'GET /cities' },
    });
    check(cities, {
      'cities 200': (r) => r.status === 200,
      'cities non-empty': (r) => Array.isArray(r.json()) && r.json().length > 0,
    });
  });
}
