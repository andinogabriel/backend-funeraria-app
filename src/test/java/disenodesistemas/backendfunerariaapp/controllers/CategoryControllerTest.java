package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.utils.CategoryTestDataFactory.getCategoryEntityWithId;
import static disenodesistemas.backendfunerariaapp.utils.CategoryTestDataFactory.getCategoryRequestDto;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class CategoryControllerTest
    extends AbstractControllerTest<CategoryRequestDto, CategoryResponseDto, CategoryEntity, Long> {

  @Mock private CategoryService categoryService;
  @InjectMocks private CategoryController sut;
  private static final Long EXISTING_CATEGORY_ID = 1L;

  @Test
  void getAllCategories() {
    testFindAll(
        () -> List.of(responseDto),
        sut::findAll,
        () -> List.of(responseDto),
        categoryService::findAll);
    then(categoryService).should(times(1)).findAll();
  }

  @Test
  void getCategoryById() {
    testFindByID(categoryService::findById, sut::findById, EXISTING_CATEGORY_ID, responseDto);
    then(categoryService).should(times(1)).findById(EXISTING_CATEGORY_ID);
  }

  @Test
  void createCategory() {
    testCreate(categoryService::create, sut::create, requestDto, responseDto);
    then(categoryService).should(times(1)).create(requestDto);
  }

  @Test
  void updateCategory() {
    testUpdate(categoryService::update, sut::update, EXISTING_CATEGORY_ID, requestDto, responseDto);
    then(categoryService).should(times(1)).update(EXISTING_CATEGORY_ID, requestDto);
  }

  @Test
  void deleteCategory() {
    testDelete(sut::delete, EXISTING_CATEGORY_ID, "DELETE CATEGORY");
    then(categoryService).should(times(1)).delete(EXISTING_CATEGORY_ID);
  }

  @Override
  protected CategoryRequestDto getRequestDto() {
    return getCategoryRequestDto();
  }

  @Override
  protected Class<CategoryResponseDto> getResponseDtoClass() {
    return CategoryResponseDto.class;
  }

  @Override
  protected CategoryEntity getEntity() {
    return getCategoryEntityWithId();
  }
}
