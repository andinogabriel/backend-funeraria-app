package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.CategoryCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;

import java.util.List;

public interface ICategory {

    List<CategoryResponseDto> getAllCategories();

    CategoryResponseDto createCategory(CategoryCreationDto category);

    CategoryResponseDto updateCategory(long id, CategoryCreationDto categoryDto);

    void deleteCategory(long id);

    CategoryEntity findCategoryById(long id);

}
