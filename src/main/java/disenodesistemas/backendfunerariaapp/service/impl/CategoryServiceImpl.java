package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.CategoryRepository;
import disenodesistemas.backendfunerariaapp.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProjectionFactory projectionFactory;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAllByOrderByName();
    }

    @Override
    @Transactional
    public CategoryResponseDto createCategory(final CategoryRequestDto category) {
        val categoryEntity = new CategoryEntity(
                category.getName(),
                category.getDescription()
        );
        return projectionFactory.createProjection(CategoryResponseDto.class, categoryRepository.save(categoryEntity));
    }

    @Override
    @Transactional
    public CategoryResponseDto updateCategory(final Long id, final CategoryRequestDto categoryDto) {
        val categoryEntity = findCategoryById(id);
        categoryEntity.setName(categoryDto.getName());
        categoryEntity.setDescription(categoryDto.getDescription());
        return projectionFactory.createProjection(CategoryResponseDto.class, categoryRepository.save(categoryEntity));
    }

    @Override
    @Transactional
    public void deleteCategory(final Long id) {
        categoryRepository.delete(findCategoryById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryEntity findCategoryById(final Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("category.error.not.found"));
    }

}
