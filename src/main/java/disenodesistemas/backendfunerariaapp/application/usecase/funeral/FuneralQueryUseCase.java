package disenodesistemas.backendfunerariaapp.application.usecase.funeral;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.FuneralPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.FuneralMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.FuneralResponseDto;
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
public class FuneralQueryUseCase {

  private static final String ASC = "asc";

  private final FuneralPersistencePort funeralPersistencePort;
  private final FuneralMapper funeralMapper;
  private final AuthenticatedUserPort authenticatedUserPort;

  @Transactional(readOnly = true)
  public List<FuneralResponseDto> findAll() {
    return funeralPersistencePort.findAllByOrderByRegisterDateDesc().stream()
        .map(funeralMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public FuneralResponseDto findById(final Long id) {
    return funeralMapper.toDto(findEntityById(id));
  }

  @Transactional(readOnly = true)
  public List<FuneralResponseDto> findFuneralsByUser() {
    return funeralPersistencePort
        .findFuneralsByUserEmail(authenticatedUserPort.getAuthenticatedEmail())
        .stream()
        .map(funeralMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public Funeral findEntityById(final Long id) {
    return funeralPersistencePort
        .findById(id)
        .orElseThrow(() -> new NotFoundException("funeral.error.not.found"));
  }

  /**
   * Server-side paginated read with per-column predicates. Mirrors the affiliates /
   * incomes / items paginated use cases.
   *
   * <ul>
   *   <li>{@code deceasedName} — substring against `firstName + ' ' + lastName`.</li>
   *   <li>{@code dni} — substring against the DNI cast to string.</li>
   *   <li>{@code receiptNumber} — substring against the funeral's receipt number.</li>
   *   <li>{@code planName} — exact match (autocomplete commit).</li>
   *   <li>{@code from} / {@code to} — inclusive bounds on {@code funeralDate}; the day
   *       boundaries get expanded to start/end of day so the operator can think in
   *       calendar days.</li>
   * </ul>
   */
  @Transactional(readOnly = true)
  public Page<FuneralResponseDto> getFuneralsPaginated(
      final int page,
      final int limit,
      final String sortBy,
      final String sortDir,
      final String deceasedName,
      final String dni,
      final String receiptNumber,
      final String planName,
      final LocalDate from,
      final LocalDate to) {
    final Pageable pageable =
        PageRequest.of(
            page,
            limit,
            Strings.CI.equals(sortDir, ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending());

    final LocalDateTime safeFrom = from == null ? null : from.atStartOfDay();
    final LocalDateTime safeTo = to == null ? null : to.atTime(LocalTime.MAX);

    final Page<Funeral> entities =
        funeralPersistencePort.search(
            blankToEmpty(deceasedName),
            blankToEmpty(dni),
            blankToEmpty(receiptNumber),
            blankToEmpty(planName),
            safeFrom,
            safeTo,
            pageable);
    return new PageImpl<>(
        entities.getContent().stream().map(funeralMapper::toDto).toList(),
        pageable,
        entities.getTotalElements());
  }

  private static String blankToEmpty(final String value) {
    return value == null ? "" : value.trim();
  }
}
