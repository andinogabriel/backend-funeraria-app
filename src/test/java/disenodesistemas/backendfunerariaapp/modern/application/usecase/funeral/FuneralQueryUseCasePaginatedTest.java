package disenodesistemas.backendfunerariaapp.modern.application.usecase.funeral;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.FuneralPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.mapping.FuneralMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.FuneralResponseDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
 * Unit-level coverage for {@link FuneralQueryUseCase#getFuneralsPaginated}. Mirrors the
 * affiliates / items paginated unit-test shape — pins the orchestration without spinning
 * up a DB; the JPQL behind it is covered by the Postgres IT.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("FuneralQueryUseCase.getFuneralsPaginated")
class FuneralQueryUseCasePaginatedTest {

  @Mock private FuneralPersistencePort port;
  @Mock private FuneralMapper mapper;
  @Mock private AuthenticatedUserPort authenticatedUserPort;

  @InjectMocks private FuneralQueryUseCase useCase;

  @Test
  @DisplayName(
      "Given blank text filters and null date bounds when invoked then text params are normalised to empty and dates pass through as-is")
  void blankFiltersNormalisedToEmpty() {
    final Funeral entity = new Funeral();
    when(port.search(
            eq(""), eq(""), eq(""), eq(""), eq(null), eq(null), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toDto(entity)).thenReturn(stubDto());

    final Page<FuneralResponseDto> result =
        useCase.getFuneralsPaginated(
            0, 10, "funeralDate", "desc", null, "  ", null, "   ", null, null);

    assertThat(result.getContent()).hasSize(1);
    verify(port)
        .search(eq(""), eq(""), eq(""), eq(""), eq(null), eq(null), any(Pageable.class));
  }

  @Test
  @DisplayName(
      "Given non-blank text filters and from/to bounds when invoked then values are trimmed and bounds expanded to start/end of day")
  void textFiltersTrimmedAndDateBoundsExpanded() {
    when(port.search(
            eq("Gomez"),
            eq("351"),
            eq("F-99001"),
            eq("Premium"),
            eq(LocalDateTime.parse("2026-05-01T00:00")),
            eq(LocalDateTime.parse("2026-05-31T23:59:59.999999999")),
            any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    useCase.getFuneralsPaginated(
        0,
        10,
        "funeralDate",
        "desc",
        "  Gomez  ",
        "351",
        "F-99001",
        "Premium",
        LocalDate.parse("2026-05-01"),
        LocalDate.parse("2026-05-31"));

    verify(port)
        .search(
            eq("Gomez"),
            eq("351"),
            eq("F-99001"),
            eq("Premium"),
            eq(LocalDateTime.parse("2026-05-01T00:00")),
            eq(LocalDateTime.parse("2026-05-31T23:59:59.999999999")),
            any(Pageable.class));
  }

  @Test
  @DisplayName(
      "Given sortDir 'ASC' (upper case) when invoked then the pageable carries an ascending sort")
  void sortDirIsCaseInsensitive() {
    when(port.search(
            eq(""), eq(""), eq(""), eq(""), eq(null), eq(null), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    useCase.getFuneralsPaginated(
        0, 10, "funeralDate", "ASC", null, null, null, null, null, null);

    final Pageable used = capturePageable();
    assertThat(used.getSort().getOrderFor("funeralDate").isAscending()).isTrue();
  }

  @Test
  @DisplayName(
      "Given a non-zero page index when invoked then the pageable carries it forward 0-indexed")
  void pageIndexIsForwardedZeroIndexed() {
    when(port.search(
            eq(""), eq(""), eq(""), eq(""), eq(null), eq(null), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    useCase.getFuneralsPaginated(2, 25, "funeralDate", "desc", null, null, null, null, null, null);

    final Pageable used = capturePageable();
    assertThat(used.getPageNumber()).isEqualTo(2);
    assertThat(used.getPageSize()).isEqualTo(25);
  }

  private Pageable capturePageable() {
    final ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(port)
        .search(eq(""), eq(""), eq(""), eq(""), eq(null), eq(null), captor.capture());
    return captor.getValue();
  }

  private static FuneralResponseDto stubDto() {
    // Use the no-arg constructor — record fields default to null which is fine for
    // identity-only assertions.
    return new FuneralResponseDto(
        null, null, null, null, null, null, null, null, null, null, null, null);
  }
}
