package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;

public interface CategoryService
    extends CommonService<CategoryResponseDto, CategoryRequestDto, Long> {

  CategoryEntity findCategoryById(Long id);
}
