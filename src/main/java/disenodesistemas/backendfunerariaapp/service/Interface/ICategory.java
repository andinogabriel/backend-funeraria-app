package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.CategoryCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;

import java.util.List;

public interface ICategory {

    List<CategoryResponseDto> getAllCategories();

    CategoryResponseDto createCategory(CategoryCreationDto category);

    CategoryResponseDto updateCategory(Long id, CategoryCreationDto categoryDto);

    void deleteCategory(Long id);

    CategoryEntity findCategoryById(Long id);

}
