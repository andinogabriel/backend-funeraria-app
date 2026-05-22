package disenodesistemas.backendfunerariaapp.application.usecase.affiliate;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.AffiliateMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.AffiliateResponseDto;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
public class AffiliateQueryUseCase {

  private static final String ASC = "asc";

  private final AffiliatePersistencePort affiliatePersistencePort;
  private final AffiliateMapper affiliateMapper;
  private final AuthenticatedUserPort authenticatedUserPort;

  @Transactional(readOnly = true)
  public List<AffiliateResponseDto> findAllByDeceasedFalse() {
    return affiliatePersistencePort.findAllByDeceasedFalseOrderByStartDateDesc().stream()
        .map(affiliateMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<AffiliateResponseDto> findAll() {
    return affiliatePersistencePort.findAllByOrderByStartDateDesc().stream()
        .map(affiliateMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public AffiliateResponseDto findById(final Integer dni) {
    return affiliateMapper.toDto(findByDni(dni));
  }

  @Transactional(readOnly = true)
  public List<AffiliateResponseDto> findAffiliatesByUser() {
    return affiliatePersistencePort
        .findByUserEmailOrderByStartDateDesc(authenticatedUserPort.getAuthenticatedEmail())
        .stream()
        .map(affiliateMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<AffiliateResponseDto> findAffiliatesByFirstNameOrLastNameOrDniContaining(
      final String valueToSearch) {
    if (StringUtils.isBlank(valueToSearch == null ? null : valueToSearch.trim())) {
      return List.of();
    }

    return affiliatePersistencePort.searchByFirstNameOrLastNameOrDni(valueToSearch.trim()).stream()
        .map(affiliateMapper::toDto)
        .toList();
  }

  /**
   * Server-side paginated read with per-column predicates. Mirrors the contract of
   * {@code IncomeQueryUseCase#getIncomesPaginated}: every filter argument is optional and
   * combines with AND semantics; the frontend's column-header menus pick which columns to
   * constrain and the request carries one parameter per active filter.
   *
   * <ul>
   *   <li>{@code firstName} / {@code lastName} — case-insensitive substring match.
   *   <li>{@code dni} — case-insensitive substring match against the DNI cast to string.
   *   <li>{@code relationshipName} — exact match on the relationship name. Frontend feeds
   *       this from an autocomplete sourced from the distinct relationship names of the
   *       currently loaded rows.
   *   <li>{@code from} / {@code to} — inclusive bounds on {@code birthDate}.
   * </ul>
   *
   * <p>The {@code deceased} flag is fixed at {@code false} for now — the affiliates page
   * lists active affiliates and the &quot;Fallecidos&quot; endpoint lives under
   * {@code /deceased}. If the operator ever needs a unified view we can promote it to a
   * tri-state parameter.
   *
   * <p>Pagination is 0-indexed (Spring Data convention) and matches Material's paginator
   * directly. No legacy 1-indexed mapping here — see {@code IncomeQueryUseCase} javadoc
   * for the rationale.
   */
  @Transactional(readOnly = true)
  public Page<AffiliateResponseDto> getAffiliatesPaginated(
      final int page,
      final int limit,
      final String sortBy,
      final String sortDir,
      final String firstName,
      final String lastName,
      final String dni,
      final String relationshipName,
      final LocalDate from,
      final LocalDate to) {
    final Pageable pageable =
        PageRequest.of(
            page,
            limit,
            Strings.CI.equals(sortDir, ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending());

    final String safeFirstName = blankToEmpty(firstName);
    final String safeLastName = blankToEmpty(lastName);
    final String safeDni = blankToEmpty(dni);
    final String safeRelationship = blankToEmpty(relationshipName);

    final Page<AffiliateEntity> entities =
        affiliatePersistencePort.search(
            false,
            safeFirstName,
            safeLastName,
            safeDni,
            safeRelationship,
            from,
            to,
            pageable);
    return new PageImpl<>(
        entities.getContent().stream().map(affiliateMapper::toDto).toList(),
        pageable,
        entities.getTotalElements());
  }

  private static String blankToEmpty(final String value) {
    return value == null ? "" : value.trim();
  }

  @Transactional(readOnly = true)
  public AffiliateEntity findByDni(final Integer dni) {
    return affiliatePersistencePort
        .findByDni(dni)
        .orElseThrow(() -> new NotFoundException("affiliate.error.not.found"));
  }
}
