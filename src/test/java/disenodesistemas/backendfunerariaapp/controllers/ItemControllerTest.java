package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.utils.ItemTestDataFactory.getItem;
import static disenodesistemas.backendfunerariaapp.utils.ItemTestDataFactory.getItemRequest;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.ItemResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.service.ItemService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

class ItemControllerTest
    extends AbstractControllerTest<ItemRequestDto, ItemResponseDto, ItemEntity, String> {

  @Mock private ItemService itemService;
  @InjectMocks private ItemController sut;
  private static final String EXISTING_ITEM_IDENTIFIER = "67ad6c26-f586-4cb2-9d5e-3fbcc3e2e8eb";

  @Test
  void findAll() {
    testFindAll(
        () -> List.of(responseDto), sut::findAll, () -> List.of(responseDto), itemService::findAll);
    then(itemService).should(times(1)).findAll();
  }

  @Test
  void findById() {
    testFindByID(itemService::findById, sut::findById, EXISTING_ITEM_IDENTIFIER, responseDto);
    then(itemService).should(times(1)).findById(EXISTING_ITEM_IDENTIFIER);
  }

  @Test
  void findItemsByCategoryId() {
    final Long categoryId = 1L;
    given(itemService.getItemsByCategoryId(categoryId)).willReturn(List.of(responseDto));
    final ResponseEntity<List<ItemResponseDto>> actualResult =
        sut.findItemsByCategoryId(categoryId);
    assertAll(
        () -> assertEquals(HttpStatus.OK, actualResult.getStatusCode()),
        () -> assertEquals(List.of(responseDto), actualResult.getBody()));
    then(itemService).should(times(1)).getItemsByCategoryId(categoryId);
  }

  @Test
  void create() {
    testCreate(itemService::create, sut::create, requestDto, responseDto);
    then(itemService).should(times(1)).create(requestDto);
  }

  @Test
  void update() {
    testUpdate(itemService::update, sut::update, EXISTING_ITEM_IDENTIFIER, requestDto, responseDto);
    then(itemService).should(times(1)).update(EXISTING_ITEM_IDENTIFIER, requestDto);
  }

  @Test
  void delete() {
    testDelete(sut::delete, EXISTING_ITEM_IDENTIFIER, "DELETE ITEM");
    then(itemService).should(times(1)).delete(EXISTING_ITEM_IDENTIFIER);
  }

  @Test
  void uploadItemImage() {
    final MockMultipartFile file =
        new MockMultipartFile(
            "file", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "image content".getBytes());
    final String ITEM_CODE = "itemCode";
    willDoNothing().given(itemService).uploadItemImage(ITEM_CODE, file);
    sut.uploadItemImage(ITEM_CODE, file);
    then(itemService).should(times(1)).uploadItemImage(ITEM_CODE, file);
  }

  @Override
  protected ItemRequestDto getRequestDto() {
    return getItemRequest();
  }

  @Override
  protected Class<ItemResponseDto> getResponseDtoClass() {
    return ItemResponseDto.class;
  }

  @Override
  protected ItemEntity getEntity() {
    return getItem();
  }
}
