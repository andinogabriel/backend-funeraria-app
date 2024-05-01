package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.CategoryTestDataFactory.getCategoryEntity;
import static disenodesistemas.backendfunerariaapp.utils.CategoryTestDataFactory.getCategoryEntityWithItems;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import disenodesistemas.backendfunerariaapp.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.exceptions.ConflictException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.CategoryRepository;
import disenodesistemas.backendfunerariaapp.utils.CategoryTestDataFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

  @Mock private CategoryRepository categoryRepository;
  @Mock private ProjectionFactory projectionFactory;
  @InjectMocks private CategoryServiceImpl sut;

  private static CategoryResponseDto categoryResponseDto;
  private static CategoryRequestDto categoryRequestDto;

  @BeforeEach
  void setUp() {
    categoryRequestDto = CategoryTestDataFactory.getCategoryRequestDto();
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    categoryResponseDto =
        projectionFactory.createProjection(CategoryResponseDto.class, getCategoryEntity());
  }

  @Test
  void getAllCategories() {
    final List<CategoryResponseDto> expected = List.of(categoryResponseDto);
    given(categoryRepository.findAllByOrderByName()).willReturn(expected);

    final List<CategoryResponseDto> result = sut.findAll();

    assertAll(
        () -> assertEquals(expected.size(), result.size()),
        () -> assertEquals(expected.get(0).getName(), result.get(0).getName()));
    verify(categoryRepository, only()).findAllByOrderByName();
  }

  @Test
  void createCategory() {
    final CategoryEntity expected = getCategoryEntity();
    given(categoryRepository.save(any(CategoryEntity.class))).willReturn(expected);
    given(projectionFactory.createProjection(CategoryResponseDto.class, expected))
        .willReturn(categoryResponseDto);

    final CategoryResponseDto result = sut.create(categoryRequestDto);

    assertAll(
        () -> assertEquals(expected.getId(), result.getId()),
        () -> assertEquals(expected.getDescription(), result.getDescription()),
        () -> assertEquals(expected.getName(), result.getName()));
    verify(categoryRepository, only()).save(any(CategoryEntity.class));
  }

  @Test
  void updateCategory() {
    final Long id = categoryRequestDto.getId();
    final CategoryEntity expected = getCategoryEntity();
    given(categoryRepository.findById(id)).willReturn(Optional.of(expected));
    given(categoryRepository.save(any(CategoryEntity.class))).willReturn(expected);
    given(projectionFactory.createProjection(CategoryResponseDto.class, expected))
        .willReturn(categoryResponseDto);

    final CategoryResponseDto result = sut.update(id, categoryRequestDto);

    assertAll(
        () -> assertEquals(expected.getId(), result.getId()),
        () -> assertEquals(expected.getDescription(), result.getDescription()),
        () -> assertEquals(expected.getName(), result.getName()));
    verify(categoryRepository, atLeastOnce()).save(any(CategoryEntity.class));
    verify(categoryRepository, atLeastOnce()).findById(id);
  }

  @Test
  void deleteCategory() {
    final Long id = categoryRequestDto.getId();
    final CategoryEntity expected = getCategoryEntity();
    given(categoryRepository.findById(id)).willReturn(Optional.of(expected));

    sut.delete(id);

    verify(categoryRepository, atLeastOnce()).delete(expected);
    verify(categoryRepository, atLeastOnce()).findById(id);
  }

  @Test
  void deleteCategoryThrowsException() {
    final Long id = categoryRequestDto.getId();
    final CategoryEntity expected = getCategoryEntityWithItems();
    given(categoryRepository.findById(id)).willReturn(Optional.of(expected));

    final ConflictException result = assertThrows(ConflictException.class, () -> sut.delete(id));

    assertAll(
        () -> assertEquals(HttpStatus.CONFLICT, result.getStatus()),
        () -> assertEquals("category.error.invalid.delete", result.getMessage()));
    verify(categoryRepository, never()).delete(expected);
    verify(categoryRepository, atLeastOnce()).findById(id);
  }

  @Test
  void findCategoryById() {
    final Long id = categoryRequestDto.getId();
    final CategoryEntity expected = getCategoryEntity();
    given(categoryRepository.findById(id)).willReturn(Optional.of(expected));

    final CategoryEntity result = sut.findCategoryById(id);

    assertAll(
        () -> assertEquals(expected.getId(), result.getId()),
        () -> assertEquals(expected.getDescription(), result.getDescription()),
        () -> assertEquals(expected.getName(), result.getName()));
    verify(categoryRepository, only()).findById(id);
  }

  @Test
  void findCategoryByIdNotFoundException() {
    final Long id = 2L;
    final NotFoundException notFoundException =
        assertThrows(NotFoundException.class, () -> sut.findCategoryById(id));

    assertAll(
        () -> assertEquals("category.error.not.found", notFoundException.getMessage()),
        () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getStatus()));
    verify(categoryRepository, only()).findById(id);
  }
}
