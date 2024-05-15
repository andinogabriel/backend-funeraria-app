package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.CategoryTestDataFactory.getCategoryEntity;
import static disenodesistemas.backendfunerariaapp.utils.CategoryTestDataFactory.getCategoryEntityWithItems;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

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
    then(categoryRepository).should(times(1)).findAllByOrderByName();
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
    then(categoryRepository).should(times(1)).save(any(CategoryEntity.class));
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
    then(categoryRepository).should(times(1)).save(any(CategoryEntity.class));
    then(categoryRepository).should(times(1)).findById(id);
  }

  @Test
  void deleteCategory() {
    final Long id = categoryRequestDto.getId();
    final CategoryEntity expected = getCategoryEntity();
    given(categoryRepository.findById(id)).willReturn(Optional.of(expected));

    sut.delete(id);

    then(categoryRepository).should(times(1)).delete(expected);
    then(categoryRepository).should(times(1)).findById(id);
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
    then(categoryRepository).should(times(1)).findById(id);
    then(categoryRepository).should(times(0)).delete(expected);
  }

  @Test
  void findById() {
    final Long id = categoryRequestDto.getId();
    final CategoryEntity expected = getCategoryEntity();
    given(categoryRepository.findById(id)).willReturn(Optional.of(expected));
    given(projectionFactory.createProjection(CategoryResponseDto.class, expected))
        .willReturn(categoryResponseDto);

    final CategoryResponseDto result = sut.findById(id);

    assertAll(
        () -> assertEquals(expected.getId(), result.getId()),
        () -> assertEquals(expected.getDescription(), result.getDescription()),
        () -> assertEquals(expected.getName(), result.getName()));
    then(categoryRepository).should(times(1)).findById(id);
    then(projectionFactory).should(times(1)).createProjection(CategoryResponseDto.class, expected);
  }

  @Test
  void findByIdNotFoundException() {
    final Long id = 2L;
    final NotFoundException notFoundException =
        assertThrows(NotFoundException.class, () -> sut.findById(id));

    assertAll(
        () -> assertEquals("category.error.not.found", notFoundException.getMessage()),
        () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getStatus()));
    then(categoryRepository).should(times(1)).findById(id);
  }
}
