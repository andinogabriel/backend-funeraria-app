package disenodesistemas.backendfunerariaapp.application.usecase.item;

import disenodesistemas.backendfunerariaapp.application.port.out.ItemPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.category.CategoryQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.ItemMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.ItemResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemQueryUseCase {

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
}
