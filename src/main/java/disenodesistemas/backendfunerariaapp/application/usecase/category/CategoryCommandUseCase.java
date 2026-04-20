package disenodesistemas.backendfunerariaapp.application.usecase.category;

import disenodesistemas.backendfunerariaapp.application.port.out.CategoryPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.CategoryMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.CategoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryCommandUseCase {

  private final CategoryPersistencePort categoryPersistencePort;
  private final CategoryMapper categoryMapper;
  private final CategoryQueryUseCase categoryQueryUseCase;

  @Transactional
  public CategoryResponseDto create(final CategoryRequestDto category) {
    final CategoryEntity categoryEntity = categoryMapper.toEntity(category);
    return categoryMapper.toDto(categoryPersistencePort.save(categoryEntity));
  }

  @Transactional
  public CategoryResponseDto update(final Long id, final CategoryRequestDto categoryDto) {
    final CategoryEntity categoryEntity = categoryQueryUseCase.findCategoryEntityById(id);
    categoryMapper.updateEntity(categoryDto, categoryEntity);
    return categoryMapper.toDto(categoryPersistencePort.save(categoryEntity));
  }

  @Transactional
  public void delete(final Long id) {
    final CategoryEntity categoryEntity = categoryQueryUseCase.findCategoryEntityById(id);
    if (!categoryEntity.getItems().isEmpty()) {
      throw new ConflictException("category.error.invalid.delete");
    }
    categoryPersistencePort.delete(categoryEntity);
  }
}
