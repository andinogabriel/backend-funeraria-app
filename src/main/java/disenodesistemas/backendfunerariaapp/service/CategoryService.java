package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.CategoryDto;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        CategoryDto categoryDto = mapper.map(categoryCreated, CategoryDto.class);
        return categoryDto;
    }

    public CategoryDto updateCategory(long id, CategoryDto categoryDto) {
        CategoryEntity categoryEntity = categoryRepository.findById(id);
        categoryEntity.setName(categoryDto.getName());
        categoryEntity.setDescription(categoryDto.getDescription());
        CategoryEntity updatedCategory = categoryRepository.save(categoryEntity);
        CategoryDto categoryToReturn = mapper.map(updatedCategory, CategoryDto.class);
        return categoryToReturn;
    }

    public void deleteCategory(long id) {
        CategoryEntity categoryEntity = categoryRepository.findById(id);
        categoryRepository.delete(categoryEntity);
    }



}