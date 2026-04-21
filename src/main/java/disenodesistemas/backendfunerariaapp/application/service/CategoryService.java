package disenodesistemas.backendfunerariaapp.application.service;

import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.CategoryResponseDto;
import java.util.List;

public interface CategoryService {

  CategoryResponseDto create(CategoryRequestDto dto);

  CategoryResponseDto update(Long id, CategoryRequestDto dto);

  void delete(Long id);

  List<CategoryResponseDto> findAll();

  CategoryResponseDto findById(Long id);

  CategoryEntity findCategoryEntityById(Long id);
}
