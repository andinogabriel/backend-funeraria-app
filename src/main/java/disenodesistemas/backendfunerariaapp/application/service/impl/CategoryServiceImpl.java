package disenodesistemas.backendfunerariaapp.application.service.impl;

import disenodesistemas.backendfunerariaapp.application.service.CategoryService;
import disenodesistemas.backendfunerariaapp.application.usecase.category.CategoryCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.category.CategoryQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.CategoryResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final CategoryCommandUseCase categoryCommandUseCase;
  private final CategoryQueryUseCase categoryQueryUseCase;

  @Override
  public List<CategoryResponseDto> findAll() {
    return categoryQueryUseCase.findAll();
  }

  @Override
  public CategoryResponseDto create(final CategoryRequestDto category) {
    return categoryCommandUseCase.create(category);
  }

  @Override
  public CategoryResponseDto update(final Long id, final CategoryRequestDto categoryDto) {
    return categoryCommandUseCase.update(id, categoryDto);
  }

  @Override
  public void delete(final Long id) {
    categoryCommandUseCase.delete(id);
  }

  @Override
  public CategoryResponseDto findById(final Long id) {
    return categoryQueryUseCase.findById(id);
  }

  @Override
  public CategoryEntity findCategoryEntityById(final Long id) {
    return categoryQueryUseCase.findCategoryEntityById(id);
  }
}

