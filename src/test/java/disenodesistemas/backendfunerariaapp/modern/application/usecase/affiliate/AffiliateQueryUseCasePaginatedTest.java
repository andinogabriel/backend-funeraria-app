package disenodesistemas.backendfunerariaapp.modern.application.usecase.affiliate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.usecase.affiliate.AffiliateQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.mapping.AffiliateMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.AffiliateResponseDto;
import java.time.LocalDate;
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
import org.springframework.data.domain.Pageable;

/**
 * Unit-level coverage for {@link AffiliateQueryUseCase#getAffiliatesPaginated}. Pinpoints the
 * interesting bits of the orchestration without spinning up a database — the JPQL behind it
 * is already covered by {@code AffiliateSearchPostgresIntegrationTest}.
 *
 * <ul>
 *   <li>blank / null string filters are normalised to {@code ""} before reaching the port
 *       (empty-string sentinel pattern, ADR-0010);</li>
 *   <li>the {@code deceased} flag is always passed as {@code false} (active affiliates only);
 *   <li>{@code sortDir} matching is case-insensitive (Spring data takes "ASC" and "asc"
 *       interchangeably so the URL stays user-friendly);</li>
 *   <li>the page index is forwarded 0-indexed — no off-by-one mapping (the income paginated
 *       endpoint shipped a bug like that, removed in master after #67).</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("AffiliateQueryUseCase.getAffiliatesPaginated")
class AffiliateQueryUseCasePaginatedTest {

  @Mock private AffiliatePersistencePort port;
  @Mock private AffiliateMapper mapper;
  @Mock private AuthenticatedUserPort authenticatedUserPort;

  @InjectMocks private AffiliateQueryUseCase useCase;

  @Test
  @DisplayName(
      "Given blank text filters and null date bounds when invoked then every text param is normalised to empty string and dates pass through as-is")
  void blankTextFiltersAreNormalisedToEmpty() {
    final AffiliateEntity entity = new AffiliateEntity();
    final AffiliateResponseDto dto =
        new AffiliateResponseDto(
            "Mariana",
            "Quiroga",
            35000001,
            LocalDate.parse("1990-01-15"),
            LocalDate.parse("2024-01-01"),
            false,
            null,
            null,
            null,
            null,
            null);
    when(port.search(
            eq(false), eq(""), eq(""), eq(""), eq(""), eq(null), eq(null), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toDto(entity)).thenReturn(dto);

    final Page<AffiliateResponseDto> result =
        useCase.getAffiliatesPaginated(
            0, 10, "lastName", "asc", null, "  ", null, "   ", null, null);

    assertThat(result.getContent()).containsExactly(dto);
    verify(port)
        .search(
            eq(false), eq(""), eq(""), eq(""), eq(""), eq(null), eq(null), any(Pageable.class));
  }

  @Test
  @DisplayName(
      "Given non-blank text filters when invoked then values are trimmed and forwarded to the port verbatim")
  void textFiltersAreTrimmedAndForwarded() {
    when(port.search(
            eq(false),
            eq("Mar"),
            eq("Quir"),
            eq("3500"),
            eq("Madre"),
            eq(LocalDate.parse("1990-01-01")),
            eq(LocalDate.parse("1999-12-31")),
            any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    useCase.getAffiliatesPaginated(
        0,
        10,
        "lastName",
        "asc",
        "  Mar  ",
        "Quir",
        "3500",
        "Madre",
        LocalDate.parse("1990-01-01"),
        LocalDate.parse("1999-12-31"));

    verify(port)
        .search(
            eq(false),
            eq("Mar"),
            eq("Quir"),
            eq("3500"),
            eq("Madre"),
            eq(LocalDate.parse("1990-01-01")),
            eq(LocalDate.parse("1999-12-31")),
            any(Pageable.class));
  }

  @Test
  @DisplayName(
      "Given sortDir 'ASC' (upper case) when invoked then the pageable carries an ascending sort — case-insensitive match")
  void sortDirIsCaseInsensitive() {
    when(port.search(
            eq(false), eq(""), eq(""), eq(""), eq(""), eq(null), eq(null), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    useCase.getAffiliatesPaginated(
        0, 10, "firstName", "ASC", null, null, null, null, null, null);

    final Pageable used = capturePageable();
    assertThat(used.getSort().getOrderFor("firstName")).isNotNull();
    assertThat(used.getSort().getOrderFor("firstName").isAscending()).isTrue();
  }

  @Test
  @DisplayName(
      "Given sortDir anything other than asc when invoked then the pageable defaults to descending — matches the income use case convention")
  void unrecognisedSortDirDefaultsToDescending() {
    when(port.search(
            eq(false), eq(""), eq(""), eq(""), eq(""), eq(null), eq(null), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    useCase.getAffiliatesPaginated(
        0, 10, "lastName", "garbage", null, null, null, null, null, null);

    final Pageable used = capturePageable();
    assertThat(used.getSort().getOrderFor("lastName").isDescending()).isTrue();
  }

  @Test
  @DisplayName(
      "Given a non-zero page index when invoked then the pageable carries it forward 0-indexed (no legacy 1-indexed mapping)")
  void pageIndexIsForwardedZeroIndexed() {
    when(port.search(
            eq(false), eq(""), eq(""), eq(""), eq(""), eq(null), eq(null), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    useCase.getAffiliatesPaginated(
        3, 10, "lastName", "asc", null, null, null, null, null, null);

    final Pageable used = capturePageable();
    assertThat(used.getPageNumber()).isEqualTo(3);
    assertThat(used.getPageSize()).isEqualTo(10);
  }

  private Pageable capturePageable() {
    final ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(port)
        .search(
            eq(false), eq(""), eq(""), eq(""), eq(""), eq(null), eq(null), captor.capture());
    return captor.getValue();
  }
}
