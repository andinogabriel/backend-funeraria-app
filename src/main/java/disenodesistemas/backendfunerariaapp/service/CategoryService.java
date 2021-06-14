package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.CategoryDto;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ModelMapper mapper;

    public List<CategoryDto> getAllCategories() {
        List<CategoryEntity> categoryEntities = categoryRepository.findAllByOrderByName();
        List<CategoryDto> categoriesDto = new ArrayList<>();

        categoryEntities.forEach(category -> {
            CategoryDto categoryDto = mapper.map(category, CategoryDto.class);
            categoriesDto.add(categoryDto);
        });
        return categoriesDto;
    }

    public CategoryDto createCategory(CategoryDto category) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setName(category.getName());
        categoryEntity.setDescription(category.getDescription());
        CategoryEntity categoryCreated = categoryRepository.save(categoryEntity);
        return mapper.map(categoryCreated, CategoryDto.class);
    }

    public CategoryDto updateCategory(long id, CategoryDto categoryDto) {
        CategoryEntity categoryEntity = findCategoryById(id);
        categoryEntity.setName(categoryDto.getName());
        categoryEntity.setDescription(categoryDto.getDescription());
        CategoryEntity updatedCategory = categoryRepository.save(categoryEntity);
        return mapper.map(updatedCategory, CategoryDto.class);
    }

    public void deleteCategory(long id) {
        CategoryEntity categoryEntity = findCategoryById(id);
        categoryRepository.delete(categoryEntity);
    }

    public CategoryDto getCategoryById(long id) {
        return mapper.map(findCategoryById(id), CategoryDto.class);
    }

    private CategoryEntity findCategoryById(long id) {
        CategoryEntity categoryEntity = categoryRepository.findById(id);
        if (categoryEntity == null) throw new RuntimeException("No existe categoría con el ID especificado.");
        return categoryEntity;
    }



}
