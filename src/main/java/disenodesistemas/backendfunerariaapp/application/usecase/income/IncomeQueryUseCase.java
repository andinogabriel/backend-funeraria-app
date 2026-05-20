package disenodesistemas.backendfunerariaapp.application.usecase.income;

import disenodesistemas.backendfunerariaapp.application.port.out.IncomePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.IncomeMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.IncomeResponseDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

  private final IncomePersistencePort incomePersistencePort;
  private final IncomeMapper incomeMapper;

  @Transactional(readOnly = true)
  public List<IncomeResponseDto> findAll() {
    return incomePersistencePort.findAllByDeletedFalseOrderByIdDesc().stream()
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
      final boolean isDeleted,
      int page,
      final int limit,
      final String sortBy,
      final String sortDir) {
    return getIncomesPaginated(isDeleted, page, limit, sortBy, sortDir, null, null, null, null);
  }

  /**
   * Filtered server-side paginated read with per-column predicates. Every filter argument
   * is optional and combines with AND semantics; the frontend's column-header menus pick
   * which columns to constrain, and the request carries one parameter per active filter.
   *
   * <ul>
   *   <li>{@code receiptNumber} — case-insensitive substring match on the income's receipt
   *       number. {@code null} or blank means "no filter".
   *   <li>{@code supplierNif} — exact match on the linked supplier's NIF. The frontend
   *       feeds this from an autocomplete (operator searches by supplier name + nif and
   *       selects one) so the filter stays a precise equality. {@code null} or blank means
   *       "any supplier (or no supplier)".
   *   <li>{@code from} / {@code to} — inclusive bounds on {@code incomeDate}. {@code null}
   *       leaves the bound open. {@code from} is expanded to the start of the day and
   *       {@code to} to the end so the operator can think in calendar days.
   * </ul>
   *
   * <p>The legacy {@code page > 0 ? page - 1 : page} mapping is preserved for backwards
   * compatibility — clients that already send 0-indexed values keep working, and that one
   * adjustment is not the right thing to clean up inside this PR.
   */
  @Transactional(readOnly = true)
  public Page<IncomeResponseDto> getIncomesPaginated(
      final boolean isDeleted,
      int page,
      final int limit,
      final String sortBy,
      final String sortDir,
      final String receiptNumber,
      final String supplierNif,
      final LocalDate from,
      final LocalDate to) {
    page = page > 0 ? page - 1 : page;
    final Pageable pageable =
        PageRequest.of(
            page,
            limit,
            Strings.CI.equals(sortDir, ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending());

    final String safeReceiptNumber = blankToEmpty(receiptNumber);
    final String safeSupplierNif = blankToEmpty(supplierNif);
    final LocalDateTime safeFrom = from == null ? null : from.atStartOfDay();
    final LocalDateTime safeTo = to == null ? null : to.atTime(LocalTime.MAX);

    final Page<IncomeEntity> entities =
        incomePersistencePort.search(
            isDeleted, safeReceiptNumber, safeSupplierNif, safeFrom, safeTo, pageable);
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
}
