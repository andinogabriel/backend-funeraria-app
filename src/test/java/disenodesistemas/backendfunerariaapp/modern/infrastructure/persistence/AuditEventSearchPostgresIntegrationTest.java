package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.domain.entity.AuditEvent;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.AuditEventRepository;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Verifies the read side of {@link AuditEventPort#search} against a live PostgreSQL container.
 * The fixture seeds a small, deterministic audit trail and exercises every filter combination
 * the controller exposes plus pagination, so the JPQL predicates and the fixed sort are covered
 * end-to-end without going through the HTTP layer.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AuditEventSearchPostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  private static final Instant T0 = Instant.parse("2026-05-01T10:00:00Z");

  @Autowired private AuditEventPort auditEventPort;
  @Autowired private AuditEventRepository auditEventRepository;

  @BeforeEach
  void seedFixture() {
    auditEventRepository.deleteAll();
    auditEventRepository.saveAll(
        List.of(
            event(T0, "admin@example.com", AuditAction.FUNERAL_CREATED, "FUNERAL", "1"),
            event(T0.plus(1, ChronoUnit.HOURS),
                "admin@example.com", AuditAction.FUNERAL_DELETED, "FUNERAL", "1"),
            event(T0.plus(2, ChronoUnit.HOURS),
                "admin@example.com", AuditAction.AFFILIATE_CREATED, "AFFILIATE", "12345678"),
            event(T0.plus(3, ChronoUnit.HOURS),
                "supervisor@example.com",
                AuditAction.USER_ROLE_GRANTED,
                "USER",
                "user@example.com"),
            event(T0.plus(4, ChronoUnit.HOURS),
                "supervisor@example.com",
                AuditAction.FUNERAL_CREATED,
                "FUNERAL",
                "2")));
  }

  @Test
  @DisplayName(
      "Given an empty filter when the search runs then every audit row is returned ordered by occurred_at descending")
  void shouldReturnAllRowsOrderedByOccurredAtDescendingWhenNoFilterIsApplied() {
    final Page<AuditEvent> page =
        auditEventPort.search(null, null, null, null, null, null, PageRequest.of(0, 10));

    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getContent())
        .extracting(AuditEvent::getOccurredAt)
        .isSortedAccordingTo(java.util.Comparator.reverseOrder());
    assertThat(page.getContent().getFirst().getAction()).isEqualTo(AuditAction.FUNERAL_CREATED);
    assertThat(page.getContent().getFirst().getTargetId()).isEqualTo("2");
  }

  @Test
  @DisplayName(
      "Given a single actor filter when the search runs then only that actor's events are returned")
  void shouldReturnOnlyEventsForTheGivenActorWhenActorEmailFilterIsApplied() {
    final Page<AuditEvent> page =
        auditEventPort.search(
            "admin@example.com", null, null, null, null, null, PageRequest.of(0, 10));

    assertThat(page.getContent())
        .hasSize(3)
        .allMatch(e -> e.getActorEmail().equals("admin@example.com"));
  }

  @Test
  @DisplayName(
      "Given an action and target type filter when the search runs then only matching rows are returned")
  void shouldCombineActionAndTargetTypeFiltersWithAndSemantics() {
    final Page<AuditEvent> page =
        auditEventPort.search(
            null,
            AuditAction.FUNERAL_CREATED,
            "FUNERAL",
            null,
            null,
            null,
            PageRequest.of(0, 10));

    assertThat(page.getContent())
        .hasSize(2)
        .allMatch(e -> e.getAction() == AuditAction.FUNERAL_CREATED)
        .allMatch(e -> e.getTargetType().equals("FUNERAL"));
  }

  @Test
  @DisplayName(
      "Given a closed time window when the search runs then only events whose occurred_at falls in the window are returned")
  void shouldRespectInclusiveBoundsOnOccurredAtWindow() {
    final Instant from = T0.plus(1, ChronoUnit.HOURS);
    final Instant to = T0.plus(2, ChronoUnit.HOURS);

    final Page<AuditEvent> page =
        auditEventPort.search(null, null, null, null, from, to, PageRequest.of(0, 10));

    assertThat(page.getContent())
        .hasSize(2)
        .extracting(AuditEvent::getAction)
        .containsExactly(AuditAction.AFFILIATE_CREATED, AuditAction.FUNERAL_DELETED);
  }

  @Test
  @DisplayName(
      "Given a small page size when the search runs then results are paginated and total count reflects the full match")
  void shouldPaginateResultsWhilePreservingTheFixedSortOrder() {
    final Page<AuditEvent> firstPage =
        auditEventPort.search(null, null, null, null, null, null, PageRequest.of(0, 2));
    final Page<AuditEvent> secondPage =
        auditEventPort.search(null, null, null, null, null, null, PageRequest.of(1, 2));

    assertThat(firstPage.getTotalElements()).isEqualTo(5);
    assertThat(firstPage.getContent()).hasSize(2);
    assertThat(secondPage.getContent()).hasSize(2);
    assertThat(firstPage.getContent().getFirst().getOccurredAt())
        .isAfter(secondPage.getContent().getFirst().getOccurredAt());
  }

  private AuditEvent event(
      final Instant occurredAt,
      final String actorEmail,
      final AuditAction action,
      final String targetType,
      final String targetId) {
    return new AuditEvent(
        occurredAt, actorEmail, 42L, action, targetType, targetId, null, null, null);
  }
}
