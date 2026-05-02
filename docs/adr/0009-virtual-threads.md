# 0009. Virtual Threads for HTTP and `@Async`

## Status

Accepted

## Context

The service is on Java 25 and Spring Boot 4. Tomcat ships with a platform-thread connector by
default (200 threads, capped); the `SimpleAsyncTaskExecutor` used for `@Async` work runs on
platform threads as well. Both choices were correct under the older `ForkJoinPool`-based
implementation of virtual threads, where common patterns — `synchronized` blocks, deep call
stacks through Spring Security, JDBC blocking calls — would pin the carrier thread and
serialize the workload.

Two upstream changes erased that constraint between us and virtual threads being a no-brainer:

- **JEP 491 (Synchronize Virtual Threads without Pinning)** finalized in JDK 24. From JDK 24
  onwards, `synchronized` blocks no longer pin the carrier thread. This used to be the single
  largest reason to defer virtual threads adoption in Spring stacks.
- **Spring Boot 4** wires virtual threads end-to-end behind a single property,
  `spring.threads.virtual.enabled`. Tomcat switches to a virtual-thread executor for the HTTP
  connector and `SimpleAsyncTaskExecutor` switches to virtual mode for `@Async` work. No code
  changes are required; the switch is reversible by flipping the property.

The remaining risks are application-level: if the codebase keeps significant amounts of state
in `ThreadLocal` slots, every short-lived virtual thread allocates and discards a fresh slot,
which is wasteful and can mask correctness bugs (state expected to persist across "the same
thread" stops persisting because every logical thread is now its own carrier). An audit was
required before adopting virtual threads broadly.

## Audit performed before adoption

- `grep -rn '\\bThreadLocal\\b\\|new\\s\\+ThreadLocal' src/main/java` returned **zero matches**
  in application code. The only `ThreadLocal` use is inside framework dependencies (Logback's
  MDC, Spring Security's `SecurityContextHolder`, Hibernate's session binding), all of which
  upstream maintainers have made virtual-thread-friendly in the versions Spring Boot 4 ships.
- `grep -rn '\\bsynchronized\\b' src/main/java` returned three call sites, all inside
  `LoginRateLimiter.State`, where the lock guards a small per-key state object held in a
  `ConcurrentHashMap`. With JEP 491 finalized, these blocks no longer pin and the per-key
  granularity rules out collateral contention even under heavy login traffic.
- The persistence layer goes through Spring Data JPA over Hikari. Hikari's blocking JDBC calls
  are exactly the workload that benefits most from virtual threads — every parked virtual
  thread frees its carrier to serve another request.
- The security path (JWT parsing, fingerprint validation, idempotency cache) only uses
  `ConcurrentMap` and immutable values. No application-level state is bound to a specific
  thread.

## Decision

Enable virtual threads end-to-end through the standard Spring Boot 4 switch.

- Add `spring.threads.virtual.enabled=true` in `application.yaml`, exposed as the
  `SPRING_THREADS_VIRTUAL_ENABLED` environment variable so a regression in production can be
  rolled back by flipping the variable without rebuilding the image.
- Add the `io.micrometer:micrometer-java21` dependency. Spring Boot 4's
  `JvmMetricsAutoConfiguration.VirtualThreadMetricsConfiguration` only registers virtual-thread
  binders when this dependency is on the classpath. The new gauges
  (`jvm.threads.virtual.pinned`, `jvm.threads.virtual.submit.failed`) flow into
  `/actuator/prometheus` and become available for dashboards and alerting.
- Do **not** introduce a thread pool of any kind on top. Virtual threads are cheap; the carrier
  pool sized to `Runtime.availableProcessors()` is the right ceiling for the JDBC blocking
  workload that dominates this service.

## Consequences

**Pros**
- Concurrency capacity is no longer bounded by Tomcat's 200-thread default. Long JDBC waits no
  longer hold a precious platform thread; under load the carrier pool serves the next request
  while the previous one is parked on the database round-trip.
- `@Async` work (currently unused but available) follows the same model: per-call virtual
  thread, no executor sizing decisions, predictable cost.
- The `VirtualThreadMetrics` binders make pinning visible. If a future change reintroduces a
  blocking native call or a long `synchronized` block on a shared monitor, the
  `jvm.threads.virtual.pinned` counter goes up and the regression surfaces in Grafana before
  it shows up as latency.
- Reversible. Setting `SPRING_THREADS_VIRTUAL_ENABLED=false` returns the service to the
  platform-thread connector without redeploying.

**Cons / trade-offs**
- Tomcat's Micrometer thread-pool metrics (`tomcat.threads.busy`, `tomcat.threads.config.max`)
  become less meaningful when the executor is virtual — the dashboards inherited from the
  pre-virtual-threads baseline keep working but a few tiles read "0 / unbounded" until the
  panels are rewritten to use `jvm.threads.virtual.pinned` and `jvm.threads.live` instead. A
  follow-up PR can refresh those panels.
- ThreadLocal-heavy diagnostic tooling attached to the JVM at runtime (some profilers, some
  legacy APM agents) can struggle with the high churn. We do not currently run any such tool;
  if we add one, the property is the kill switch.
- The audit guarantee (no application `ThreadLocal`, only short `synchronized` blocks on
  per-key state) is a static fact about today's code. New code introducing wide
  `ThreadLocal`-per-thread state or long `synchronized` regions on shared monitors must be
  flagged in code review. ArchUnit cannot enforce this directly; the deciding signal is the
  `jvm.threads.virtual.pinned` metric in production.

## Validation

- `mvn verify` with the property enabled — full test suite passes including the JaCoCo,
  ArchUnit and Checkstyle gates.
- Manual smoke check: `docker compose up --build`, hit a few authenticated endpoints, confirm
  the response headers, structured logs and JSON output remain identical to the platform-
  thread baseline. Inspect `/actuator/prometheus` for the new `jvm.threads.virtual.*` series.
- Future regression detection: alert on a non-zero rate of
  `jvm.threads.virtual.pinned_total` once the rule is added to Prometheus.

## References

- JEP 491 — Synchronize Virtual Threads without Pinning:
  https://openjdk.org/jeps/491
- Spring Boot reference, Virtual Threads:
  https://docs.spring.io/spring-boot/reference/features/task-execution-and-scheduling.html#features.task-execution-and-scheduling.virtual-threads
- Micrometer virtual thread metrics:
  https://docs.micrometer.io/micrometer/reference/reference/jvm.html
