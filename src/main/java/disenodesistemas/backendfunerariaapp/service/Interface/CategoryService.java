package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;

import java.util.List;

public interface CategoryService {

    List<CategoryResponseDto> getAllCategories();

    CategoryResponseDto createCategory(CategoryRequestDto category);

    CategoryResponseDto updateCategory(Long id, CategoryRequestDto categoryDto);

    void deleteCategory(Long id);

    CategoryEntity findCategoryById(Long id);

}
