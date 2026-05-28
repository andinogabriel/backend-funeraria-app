package disenodesistemas.backendfunerariaapp.application.usecase.income;

import disenodesistemas.backendfunerariaapp.application.port.out.IncomePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.IncomeStatus;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.IncomeMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.IncomeResponseDto;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IncomeQueryUseCase {

  private static final String ASC = "asc";
  /**
   * Timezone used to bracket the operator's date-range filter into absolute
   * UTC instants. See {@code REPORTING_ZONE} in the previous revision for the
   * full rationale — the funeral home runs out of Argentina (UTC-3, no DST).
   */
  private static final ZoneId REPORTING_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

  private final IncomePersistencePort incomePersistencePort;
  private final IncomeMapper incomeMapper;

  @Transactional(readOnly = true)
  public List<IncomeResponseDto> findAll() {
    return incomePersistencePort.findAllActiveOrderByIdDesc().stream()
        .map(incomeMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public IncomeResponseDto findById(final Long receiptNumber) {
    return incomeMapper.toDto(findEntityByReceiptNumber(receiptNumber));
  }

  @Transactional(readOnly = true)
  public IncomeResponseDto findByReceiptNumber(final Long receiptNumber) {
    return incomeMapper.toDto(findEntityByReceiptNumber(receiptNumber));
  }

  @Transactional(readOnly = true)
  public Page<IncomeResponseDto> getIncomesPaginated(
      final IncomeStatus status,
      int page,
      final int limit,
      final String sortBy,
      final String sortDir) {
    return getIncomesPaginated(status, page, limit, sortBy, sortDir, null, null, null, null);
  }

  /**
   * Filtered server-side paginated read with per-column predicates.
   *
   * <ul>
   *   <li>{@code status} — when {@code null}, returns rows of every lifecycle state (the
   *       UI's "Todas" filter). When set, restricts to that exact status: {@code ACTIVE}
   *       for the regular operator view, {@code ANNULLED} for the cancelled-receipts
   *       audit view. Reversal counter-entries always carry {@code ACTIVE}.
   *   <li>{@code receiptNumber} — case-insensitive substring match.
   *   <li>{@code supplierNif} — exact match (frontend autocomplete commit).
   *   <li>{@code from} / {@code to} — inclusive bounds on {@code incomeDate}, expanded
   *       to start / end of the operator-picked calendar day in Argentina local time.
   * </ul>
   */
  @Transactional(readOnly = true)
  public Page<IncomeResponseDto> getIncomesPaginated(
      final IncomeStatus status,
      final int page,
      final int limit,
      final String sortBy,
      final String sortDir,
      final String receiptNumber,
      final String supplierNif,
      final LocalDate from,
      final LocalDate to) {
    final Pageable pageable =
        PageRequest.of(
            page,
            limit,
            Strings.CI.equals(sortDir, ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending());

    final String safeReceiptNumber = blankToEmpty(receiptNumber);
    final String safeSupplierNif = blankToEmpty(supplierNif);
    final Instant safeFrom = from == null ? null : from.atStartOfDay(REPORTING_ZONE).toInstant();
    final Instant safeTo =
        to == null ? null : to.atTime(LocalTime.MAX).atZone(REPORTING_ZONE).toInstant();

    final Page<IncomeEntity> entities =
        incomePersistencePort.search(
            status, safeReceiptNumber, safeSupplierNif, safeFrom, safeTo, pageable);
    return new PageImpl<>(
        entities.getContent().stream().map(incomeMapper::toDto).toList(),
        pageable,
        entities.getTotalElements());
  }

  private static String blankToEmpty(final String value) {
    return value == null ? "" : value.trim();
  }

  @Transactional(readOnly = true)
  public IncomeEntity findEntityByReceiptNumber(final Long receiptNumber) {
    return incomePersistencePort
        .findByReceiptNumber(receiptNumber)
        .orElseThrow(() -> new NotFoundException("income.error.not.found"));
  }

  /** Primary-key lookup used by the annul flow. */
  @Transactional(readOnly = true)
  public IncomeEntity findEntityById(final Long id) {
    return incomePersistencePort
        .findById(id)
        .orElseThrow(() -> new NotFoundException("income.error.not.found"));
  }
}
