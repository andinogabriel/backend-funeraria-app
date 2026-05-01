# 0006. Caffeine Cache for Catalog Lookups

## Status

Accepted

## Context

The service exposes a set of small, read-heavy catalog endpoints that are called constantly by
the frontend and by transactional flows: `brand`, `category`, `death-cause`, `gender`,
`province`, `city`, `relationship`, `receipt-type` and `role`. Each request hits PostgreSQL,
performs a simple `SELECT ... ORDER BY name` (or `findById`), and returns a small list mapped to
DTOs.

The data behind these endpoints changes very rarely. Read-only catalogs (`gender`, `province`,
`city`, `relationship`, `receipt-type`, `role`) are seeded by Flyway migrations and never written
to from the API. Mutable catalogs (`brand`, `category`, `death-cause`) are edited by admins
through the API but write traffic is negligible compared to read traffic.

Profiling local requests shows the catalog endpoints are dominated by the database round-trip
plus the JPA-to-DTO mapping. The combination of high read frequency, low write rate, and small
result sets makes them an ideal target for in-process memoization.

## Decision

Enable Spring's caching abstraction backed by **Caffeine** and apply it to catalog query use
cases.

- Add `spring-boot-starter-cache` and `com.github.ben-manes.caffeine:caffeine` to the build.
- Provide a single `CacheConfig` class with `@EnableCaching` and a `CaffeineCacheManager`
  configured with a shared default specification: `maximumSize=500, expireAfterWrite=10m`. The
  manager creates caches lazily by name so adding a new catalog cache does not require touching
  this class.
- Annotate every catalog query use case method that returns DTOs with `@Cacheable`, using one
  cache name per aggregate (e.g. `catalog.brand`, `catalog.gender`). Methods that return JPA
  entities are intentionally **not** cached — entities are managed objects whose detached state
  can cause issues, and they are only consumed internally by command use cases inside an active
  transaction where the cache would not help anyway.
- For aggregates that have command use cases (`brand`, `category`, `death-cause`), annotate
  every `create` / `update` / `delete` method with
  `@CacheEvict(value = ..., allEntries = true)`. Catalogs are small, so a coarse-grained
  eviction is correct and removes the risk of stale `findAll` views after a single edit.
- For `city`, which exposes both `findById` and `findByProvinceId`, use two distinct cache names
  (`catalog.city.byId` and `catalog.city.byProvince`) instead of forcing a composite key. Both
  caches are read-only since cities are seeded.
- Cache names are exposed as `public static final String` constants on `CacheConfig` so use
  cases reference them through the constant rather than a string literal.

## Consequences

**Pros**
- Catalog endpoint latency drops to in-memory map lookup time after the first request, removing
  the database round-trip and the JPA mapping cost on the hot path.
- The cache manager is a single Spring bean and the per-aggregate cache names are tiny:
  introducing a new catalog cache is a one-line `@Cacheable` annotation plus a constant.
- Read-only catalogs never need eviction code, so they cannot be invalidated by accident.
- Coarse `allEntries = true` eviction on writes makes correctness reasoning simple: a write
  invalidates the whole cache for that aggregate, including both `findAll` and per-id entries.

**Cons / trade-offs**
- The cache is local to each JVM. In a multi-instance deployment, different replicas will each
  warm their own cache and may briefly serve different snapshots after a write hits one
  instance. This is acceptable here because the modular monolith currently runs as a single
  instance per environment; revisit if/when we scale out.
- A 10-minute write expiry means an external write that bypassed the API (a manual SQL change,
  a Flyway migration touching catalog rows on next boot) would not be visible to running
  instances until the entry expires. This matches the existing behavior of any in-memory cache
  and is acceptable for catalog data.
- Caching `@Cacheable` is AOP-based, so calls from within the same use case bean to a cached
  method on the same instance bypass the proxy. This is intentional in our design: the only
  callers we want to memoize are the controllers (which always go through the proxy) and the
  proxied bean references injected into command use cases.

## Validation

`CatalogCacheBehaviorTest` (in
`src/test/java/.../modern/application/usecase/catalog/`) starts a small Spring context that
includes `CacheConfig` plus the brand and gender use cases with mocked persistence ports, and
verifies:

- repeated `findAll()` and `findById()` calls hit the persistence port only once;
- distinct ids are cached independently;
- `create` / `update` / `delete` invalidate the cache so the next read goes back to the port;
- read-only catalogs (Gender) cache without needing eviction.

## References

- Spring Framework caching abstraction:
  https://docs.spring.io/spring-framework/reference/integration/cache.html
- Caffeine cache: https://github.com/ben-manes/caffeine
