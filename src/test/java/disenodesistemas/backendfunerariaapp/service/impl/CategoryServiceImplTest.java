package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.CategoryDtoMother;
import disenodesistemas.backendfunerariaapp.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntityMother;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProjectionFactory projectionFactory;
    @InjectMocks
    private CategoryServiceImpl sut;

    private CategoryResponseDto categoryResponseDto;

    @BeforeEach
    void setUp() {
        final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
        categoryResponseDto = projectionFactory.createProjection(CategoryResponseDto.class, CategoryEntityMother.getCategoryEntity());
    }

    @Test
    void getAllCategories() {
        final List<CategoryResponseDto> expected = List.of(categoryResponseDto);
        given(categoryRepository.findAllByOrderByName()).willReturn(expected);

        final List<CategoryResponseDto> result = sut.getAllCategories();

        verify(categoryRepository, times(1)).findAllByOrderByName();
        assertEquals(expected.size(), result.size());
        assertEquals(expected.get(0).getName(), result.get(0).getName());
    }

    @Test
    void createCategory() {
        final CategoryEntity expected = CategoryEntityMother.getCategoryEntity();
        given(categoryRepository.save(any(CategoryEntity.class))).willReturn(expected);
        given(projectionFactory.createProjection(CategoryResponseDto.class, expected)).willReturn(categoryResponseDto);

        final CategoryResponseDto result = sut.createCategory(CategoryDtoMother.getCategoryRequestDto());

        assertAll(
                () -> assertEquals(expected.getId(), result.getId()),
                () -> assertEquals(expected.getDescription(), result.getDescription()),
                () -> assertEquals(expected.getName(), result.getName())
        );
        verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
    }

    @Test
    void updateCategory() {
        final Long id = CategoryDtoMother.getCategoryRequestDto().getId();
        final CategoryEntity expected = CategoryEntityMother.getCategoryEntity();
        given(categoryRepository.findById(id)).willReturn(Optional.of(expected));
        given(categoryRepository.save(any(CategoryEntity.class))).willReturn(expected);
        given(projectionFactory.createProjection(CategoryResponseDto.class, expected)).willReturn(categoryResponseDto);

        final CategoryResponseDto result = sut.updateCategory(id, CategoryDtoMother.getCategoryRequestDto());

        assertAll(
                () -> assertEquals(expected.getId(), result.getId()),
                () -> assertEquals(expected.getDescription(), result.getDescription()),
                () -> assertEquals(expected.getName(), result.getName())
        );
        verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
        verify(categoryRepository, times(1)).findById(id);
    }

    @Test
    void deleteCategory() {
        final Long id = CategoryDtoMother.getCategoryRequestDto().getId();
        final CategoryEntity expected = CategoryEntityMother.getCategoryEntity();
        given(categoryRepository.findById(id)).willReturn(Optional.of(expected));

        sut.deleteCategory(id);

        verify(categoryRepository, times(1)).delete(expected);
        verify(categoryRepository, times(1)).findById(id);
    }

    @Test
    void findCategoryById() {
        final Long id = CategoryDtoMother.getCategoryRequestDto().getId();
        final CategoryEntity expected = CategoryEntityMother.getCategoryEntity();
        given(categoryRepository.findById(id)).willReturn(Optional.of(expected));

        final CategoryEntity result = sut.findCategoryById(id);

        assertAll(
                () -> assertEquals(expected.getId(), result.getId()),
                () -> assertEquals(expected.getDescription(), result.getDescription()),
                () -> assertEquals(expected.getName(), result.getName())
        );
        verify(categoryRepository, times(1)).findById(id);
    }

    @Test
    void findCategoryByIdNotFoundException() {
        final Long id = 2L;
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> sut.findCategoryById(id));

        assertAll(
                () -> assertEquals("category.error.not.found", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getStatus())
        );
        verify(categoryRepository, times(1)).findById(id);
    }
}