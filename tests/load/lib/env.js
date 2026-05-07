// Shared configuration for every k6 scenario. Reads sensible defaults from environment
// variables so the same scripts can target a local docker-compose stack (`SERVER_PORT=8081`
// per docker-compose.yml) or a deployed environment without code changes.

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';

// Bootstrap admin credentials documented in docker-compose.yml. Override in CI/staging via
// K6_ADMIN_EMAIL / K6_ADMIN_PASSWORD so production-like passwords never live in git.
export const ADMIN_EMAIL = __ENV.K6_ADMIN_EMAIL || 'admin@funeraria.local';
export const ADMIN_PASSWORD = __ENV.K6_ADMIN_PASSWORD || 'Admin123!';

// Centralized JSON content type so every script sends the same headers and the recorded
// metrics stay comparable across runs.
export const JSON_HEADERS = {
  'Content-Type': 'application/json',
  Accept: 'application/json',
};
