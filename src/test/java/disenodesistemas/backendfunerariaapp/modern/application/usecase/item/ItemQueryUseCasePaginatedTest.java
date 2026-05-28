package disenodesistemas.backendfunerariaapp.modern.application.usecase.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.ItemPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.category.CategoryQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.item.ItemQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.mapping.ItemMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.ItemResponseDto;
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
 * Unit-level coverage for {@link ItemQueryUseCase#getItemsPaginated}. Mirrors the
 * `AffiliateQueryUseCasePaginatedTest` shape — pins the orchestration without spinning
 * up a DB; the JPQL behind it is covered by {@code ItemSearchPostgresIntegrationTest}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("ItemQueryUseCase.getItemsPaginated")
class ItemQueryUseCasePaginatedTest {

  @Mock private ItemPersistencePort port;
  @Mock private ItemMapper mapper;
  @Mock private CategoryQueryUseCase categoryQueryUseCase;

  @InjectMocks private ItemQueryUseCase useCase;

  @Test
  @DisplayName(
      "Given blank text filters when invoked then every text param is normalised to empty string")
  void blankTextFiltersAreNormalisedToEmpty() {
    final ItemEntity entity = new ItemEntity();
    final ItemResponseDto dto =
        new ItemResponseDto(
            "Cofre Standard",
            null,
            "COF-001",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    when(port.search(eq(""), eq(""), eq(""), eq(""), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toDto(entity)).thenReturn(dto);

    final Page<ItemResponseDto> result =
        useCase.getItemsPaginated(0, 10, "name", "asc", null, "  ", null, "");

    assertThat(result.getContent()).containsExactly(dto);
    verify(port).search(eq(""), eq(""), eq(""), eq(""), any(Pageable.class));
  }

  @Test
  @DisplayName(
      "Given non-blank text filters when invoked then values are trimmed and forwarded to the port verbatim")
  void textFiltersAreTrimmedAndForwarded() {
    when(port.search(eq("COF"), eq("Cofre"), eq("Cofres"), eq("Akme"), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    useCase.getItemsPaginated(0, 10, "name", "asc", " COF ", "Cofre", "Cofres", "Akme");

    verify(port)
        .search(eq("COF"), eq("Cofre"), eq("Cofres"), eq("Akme"), any(Pageable.class));
  }

  @Test
  @DisplayName(
      "Given sortDir 'ASC' (upper case) when invoked then the pageable carries an ascending sort")
  void sortDirIsCaseInsensitive() {
    when(port.search(eq(""), eq(""), eq(""), eq(""), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    useCase.getItemsPaginated(0, 10, "name", "ASC", null, null, null, null);

    final Pageable used = capturePageable();
    assertThat(used.getSort().getOrderFor("name").isAscending()).isTrue();
  }

  @Test
  @DisplayName(
      "Given a non-zero page index when invoked then the pageable carries it forward 0-indexed")
  void pageIndexIsForwardedZeroIndexed() {
    when(port.search(eq(""), eq(""), eq(""), eq(""), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    useCase.getItemsPaginated(2, 25, "name", "asc", null, null, null, null);

    final Pageable used = capturePageable();
    assertThat(used.getPageNumber()).isEqualTo(2);
    assertThat(used.getPageSize()).isEqualTo(25);
  }

  private Pageable capturePageable() {
    final ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(port).search(eq(""), eq(""), eq(""), eq(""), captor.capture());
    return captor.getValue();
  }
}
