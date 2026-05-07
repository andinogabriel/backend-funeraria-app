package disenodesistemas.backendfunerariaapp.modern.application.usecase.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.usecase.audit.AuditEventFilter;
import disenodesistemas.backendfunerariaapp.application.usecase.audit.AuditEventQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.AuditEvent;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.mapping.AuditEventMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.AuditEventResponseDto;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("AuditEventQueryUseCase")
class AuditEventQueryUseCaseTest {

  @Mock private AuditEventPort auditEventPort;
  @Mock private AuditEventMapper auditEventMapper;

  @InjectMocks private AuditEventQueryUseCase auditEventQueryUseCase;

  @Test
  @DisplayName(
      "Given a filter and a one-based page when the use case is invoked then the port receives every criterion forwarded as-is and a zero-based pageable")
  void givenAFilterAndAOneBasedPageWhenTheUseCaseIsInvokedThenThePortReceivesEveryCriterionForwardedAsIsAndAZeroBasedPageable() {
    final Instant from = Instant.parse("2026-05-01T00:00:00Z");
    final Instant to = Instant.parse("2026-05-07T23:59:59Z");
    final AuditEventFilter filter =
        new AuditEventFilter(
            "admin@example.com", AuditAction.FUNERAL_CREATED, "FUNERAL", "1", from, to);
    final AuditEvent entity = auditEvent(AuditAction.FUNERAL_CREATED);
    final AuditEventResponseDto dto = response(AuditAction.FUNERAL_CREATED);

    when(auditEventPort.search(
            "admin@example.com",
            AuditAction.FUNERAL_CREATED,
            "FUNERAL",
            "1",
            from,
            to,
            PageRequest.of(0, 10)))
        .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1));
    when(auditEventMapper.toDto(entity)).thenReturn(dto);

    final Page<AuditEventResponseDto> result = auditEventQueryUseCase.search(filter, 1, 10);

    assertThat(result.getContent()).containsExactly(dto);
    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  @DisplayName(
      "Given a non-positive page size when the use case is invoked then the default page size of 25 is used")
  void givenANonPositivePageSizeWhenTheUseCaseIsInvokedThenTheDefaultPageSizeOf25IsUsed() {
    when(auditEventPort.search(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of()));

    auditEventQueryUseCase.search(AuditEventFilter.empty(), 1, 0);

    final Pageable used = capturePageable();
    assertThat(used.getPageSize()).isEqualTo(25);
    assertThat(used.getPageNumber()).isZero();
  }

  @Test
  @DisplayName(
      "Given a page size above the maximum when the use case is invoked then the size is clamped to 100")
  void givenAPageSizeAboveTheMaximumWhenTheUseCaseIsInvokedThenTheSizeIsClampedTo100() {
    when(auditEventPort.search(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of()));

    auditEventQueryUseCase.search(AuditEventFilter.empty(), 3, 500);

    final Pageable used = capturePageable();
    assertThat(used.getPageSize()).isEqualTo(100);
    assertThat(used.getPageNumber()).isEqualTo(2);
  }

  @Test
  @DisplayName(
      "Given a zero or negative one-based page when the use case is invoked then the request is coerced to the first page")
  void givenAZeroOrNegativeOneBasedPageWhenTheUseCaseIsInvokedThenTheRequestIsCoercedToTheFirstPage() {
    when(auditEventPort.search(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of()));

    auditEventQueryUseCase.search(AuditEventFilter.empty(), 0, 25);

    final Pageable used = capturePageable();
    assertThat(used.getPageNumber()).isZero();
    assertThat(used.getPageSize()).isEqualTo(25);
  }

  private Pageable capturePageable() {
    final ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(auditEventPort)
        .search(any(), any(), any(), any(), any(), any(), captor.capture());
    return captor.getValue();
  }

  private AuditEvent auditEvent(final AuditAction action) {
    return new AuditEvent(
        Instant.parse("2026-05-07T12:00:00Z"),
        "admin@example.com",
        42L,
        action,
        "FUNERAL",
        "1",
        "trace-id",
        "corr-id",
        "{\"k\":\"v\"}");
  }

  private AuditEventResponseDto response(final AuditAction action) {
    return new AuditEventResponseDto(
        100L,
        Instant.parse("2026-05-07T12:00:00Z"),
        "admin@example.com",
        42L,
        action,
        "FUNERAL",
        "1",
        "trace-id",
        "corr-id",
        "{\"k\":\"v\"}");
  }
}
