package disenodesistemas.backendfunerariaapp.application.usecase.category;

import disenodesistemas.backendfunerariaapp.application.port.out.CategoryPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.CategoryMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.CategoryResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryQueryUseCase {

  private final CategoryPersistencePort categoryPersistencePort;
  private final CategoryMapper categoryMapper;

  @Transactional(readOnly = true)
  public List<CategoryResponseDto> findAll() {
    return categoryPersistencePort.findAllByOrderByName().stream().map(categoryMapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public CategoryResponseDto findById(final Long id) {
    return categoryMapper.toDto(findCategoryEntityById(id));
  }

  @Transactional(readOnly = true)
  public CategoryEntity findCategoryEntityById(final Long id) {
    return categoryPersistencePort
        .findById(id)
        .orElseThrow(() -> new NotFoundException("category.error.not.found"));
  }
}
