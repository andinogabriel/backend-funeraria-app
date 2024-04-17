package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.dto.CategoryDtoMother.getCategoryRequestDto;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import disenodesistemas.backendfunerariaapp.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.repository.CategoryRepository;
import disenodesistemas.backendfunerariaapp.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Transactional
@ActiveProfiles("test")
class CategoryServiceIntegrationTest {

  @Autowired private CategoryService categoryService;

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private ProjectionFactory projectionFactory;

  private CategoryRequestDto categoryRequestDto;

  @BeforeEach
  void setUp() {
    this.categoryRequestDto = getCategoryRequestDto();
  }

  @Test
  void testFindAll() {
    CategoryEntity category1 = new CategoryEntity("Category 1", "Description 1");
    CategoryEntity category2 = new CategoryEntity("Category 2", "Description 2");
    categoryRepository.save(category1);
    categoryRepository.save(category2);

    List<CategoryResponseDto> categories = categoryService.findAll();

    assertEquals(2, categories.size());
    assertTrue(categories.stream().anyMatch(category -> category.getName().equals("Category 1")));
    assertTrue(categories.stream().anyMatch(category -> category.getName().equals("Category 2")));
  }

  @Test
  void testCreate() {
    CategoryResponseDto actualResponse = categoryService.create(categoryRequestDto);

    assertNotNull(actualResponse);
    assertEquals(categoryRequestDto.getName(), actualResponse.getName());
    assertEquals(categoryRequestDto.getDescription(), actualResponse.getDescription());
  }

  @Test
  void testUpdate() {
    final CategoryEntity category = new CategoryEntity("Category", "Description");
    categoryRepository.save(category);
    final Long categoryId = category.getId();

    CategoryResponseDto responseDto = categoryService.update(categoryId, categoryRequestDto);

    assertAll(
        () -> assertNotNull(responseDto),
        () -> assertEquals(categoryRequestDto.getName(), responseDto.getName()),
        () -> assertEquals(categoryRequestDto.getDescription(), responseDto.getDescription()));
  }

  @Test
  void testDelete() {
    // Given
    CategoryEntity category = new CategoryEntity("Category", "Description");
    categoryRepository.save(category);
    Long categoryId = category.getId();

    // When
    assertDoesNotThrow(() -> categoryService.delete(categoryId));

    // Then
    assertFalse(categoryRepository.existsById(categoryId));
  }

  @Test
  void testFindCategoryById() {
    // Given
    CategoryEntity category = new CategoryEntity("Category", "Description");
    categoryRepository.save(category);
    Long categoryId = category.getId();

    // When
    CategoryEntity foundCategory = categoryService.findCategoryById(categoryId);

    // Then
    assertNotNull(foundCategory);
    assertEquals(category.getName(), foundCategory.getName());
    assertEquals(category.getDescription(), foundCategory.getDescription());
  }
}
