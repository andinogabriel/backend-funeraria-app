package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.exceptions.ConflictException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.CategoryRepository;
import disenodesistemas.backendfunerariaapp.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;
  private final ProjectionFactory projectionFactory;

  @Override
  @Transactional(readOnly = true)
  public List<CategoryResponseDto> findAll() {
    return categoryRepository.findAllByOrderByName();
  }

  @Override
  @Transactional
  public CategoryResponseDto create(final CategoryRequestDto category) {
    val categoryEntity =
        new CategoryEntity(StringUtils.capitalize(category.getName()), category.getDescription());
    return projectionFactory.createProjection(
        CategoryResponseDto.class, categoryRepository.save(categoryEntity));
  }

  @Override
  @Transactional
  public CategoryResponseDto update(final Long id, final CategoryRequestDto categoryDto) {
    val categoryEntity = findCategoryEntityById(id);
    categoryEntity.setName(categoryDto.getName());
    categoryEntity.setDescription(categoryDto.getDescription());
    return projectionFactory.createProjection(
        CategoryResponseDto.class, categoryRepository.save(categoryEntity));
  }

  @Override
  @Transactional
  public void delete(final Long id) {
    final CategoryEntity categoryEntity = findCategoryEntityById(id);
    if (!categoryEntity.getItems().isEmpty())
      throw new ConflictException("category.error.invalid.delete");
    categoryRepository.delete(categoryEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public CategoryResponseDto findById(final Long id) {
    return categoryRepository
        .findById(id)
        .map(
            categoryEntity ->
                projectionFactory.createProjection(CategoryResponseDto.class, categoryEntity))
        .orElseThrow(() -> new NotFoundException("category.error.not.found"));
  }

  @Override
  @Transactional(readOnly = true)
  public CategoryEntity findCategoryEntityById(final Long id) {
    return categoryRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("category.error.not.found"));
  }
}
