package disenodesistemas.backendfunerariaapp.application.usecase.item;

import disenodesistemas.backendfunerariaapp.application.port.out.ItemPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.category.CategoryQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.ItemMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.ItemResponseDto;
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
public class ItemQueryUseCase {

  private static final String ASC = "asc";

  private final ItemPersistencePort itemPersistencePort;
  private final ItemMapper itemMapper;
  private final CategoryQueryUseCase categoryQueryUseCase;

  @Transactional(readOnly = true)
  public List<ItemResponseDto> findAll() {
    return itemPersistencePort.findAll().stream().map(itemMapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<ItemResponseDto> getItemsByCategoryId(final Long id) {
    return itemPersistencePort.findByCategoryOrderByName(categoryQueryUseCase.findCategoryEntityById(id)).stream()
        .map(itemMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public ItemResponseDto findById(final String code) {
    return itemMapper.toDto(getItemByCode(code));
  }

  @Transactional(readOnly = true)
  public ItemEntity getItemByCode(final String code) {
    return itemPersistencePort
        .findByCode(code)
        .orElseThrow(() -> new NotFoundException("item.error.code.not.found"));
  }

  /**
   * Server-side paginated read with per-column predicates. Mirrors
   * {@link disenodesistemas.backendfunerariaapp.application.usecase.affiliate.AffiliateQueryUseCase#getAffiliatesPaginated}
   * — every filter argument is optional and combines with AND semantics; the frontend's
   * column-header menus pick which columns to constrain.
   *
   * <ul>
   *   <li>{@code code} / {@code name} — case-insensitive substring match.
   *   <li>{@code categoryName} / {@code brandName} — exact match. Frontend feeds these
   *       through autocomplete columns sourced from the distinct category / brand names
   *       of the currently loaded rows.
   * </ul>
   */
  @Transactional(readOnly = true)
  public Page<ItemResponseDto> getItemsPaginated(
      final int page,
      final int limit,
      final String sortBy,
      final String sortDir,
      final String code,
      final String name,
      final String categoryName,
      final String brandName) {
    final Pageable pageable =
        PageRequest.of(
            page,
            limit,
            Strings.CI.equals(sortDir, ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending());

    final Page<ItemEntity> entities =
        itemPersistencePort.search(
            blankToEmpty(code),
            blankToEmpty(name),
            blankToEmpty(categoryName),
            blankToEmpty(brandName),
            pageable);
    return new PageImpl<>(
        entities.getContent().stream().map(itemMapper::toDto).toList(),
        pageable,
        entities.getTotalElements());
  }

  private static String blankToEmpty(final String value) {
    return value == null ? "" : value.trim();
  }
}
