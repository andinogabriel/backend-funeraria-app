package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.CategoryCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.CategoryRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.ICategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class CategoryServiceImpl implements ICategory {

    private final MessageSource messageSource;
    private final CategoryRepository categoryRepository;
    private final ProjectionFactory projectionFactory;

    @Autowired
    public CategoryServiceImpl(MessageSource messageSource, CategoryRepository categoryRepository, ProjectionFactory projectionFactory) {
        this.messageSource = messageSource;
        this.categoryRepository = categoryRepository;
        this.projectionFactory = projectionFactory;
    }


    @Override
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAllByOrderByName();
    }

    @Override
    public CategoryResponseDto createCategory(CategoryCreationDto category) {
        CategoryEntity categoryEntity = new CategoryEntity(category.getName(), category.getDescription());
        return projectionFactory.createProjection(CategoryResponseDto.class, categoryEntity);
    }

    @Override
    public CategoryResponseDto updateCategory(Long id, CategoryCreationDto categoryDto) {
        CategoryEntity categoryEntity = findCategoryById(id);
        categoryEntity.setName(categoryDto.getName());
        categoryEntity.setDescription(categoryDto.getDescription());
        CategoryEntity updatedCategory = categoryRepository.save(categoryEntity);
        return projectionFactory.createProjection(CategoryResponseDto.class, updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        CategoryEntity categoryEntity = findCategoryById(id);
        categoryRepository.delete(categoryEntity);
    }

    @Override
    public CategoryEntity findCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new AppException(
                        messageSource.getMessage("category.error.not.found", null, Locale.getDefault()),
                        HttpStatus.NOT_FOUND
                )
        );
    }

}
