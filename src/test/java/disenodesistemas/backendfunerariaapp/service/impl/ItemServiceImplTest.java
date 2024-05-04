package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.ItemTestDataFactory.getItem;
import static disenodesistemas.backendfunerariaapp.utils.ItemTestDataFactory.getItemRequest;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.ItemResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import disenodesistemas.backendfunerariaapp.service.CategoryService;
import disenodesistemas.backendfunerariaapp.service.FileStoreService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

  @Mock private ItemRepository itemRepository;
  @Mock private CategoryService categoryService;
  @Mock private FileStoreService fileStoreService;
  @Mock private ProjectionFactory projectionFactory;
  @Spy private ModelMapper mapper;
  @InjectMocks private ItemServiceImpl sut;

  private ItemRequestDto itemRequestDto;
  private ItemResponseDto itemResponseDto;
  private ItemEntity itemEntity;
  private static final String EXISTING_ITEM_CODE = "itemCode";

  @BeforeEach
  void setUp() {
    itemRequestDto = getItemRequest();
    itemEntity = getItem();
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    itemResponseDto = projectionFactory.createProjection(ItemResponseDto.class, itemEntity);
  }

  @Test
  void findAll() {
    final List<ItemResponseDto> expectedResult = List.of(itemResponseDto);
    given(itemRepository.findAllProjectedBy()).willReturn(expectedResult);

    final List<ItemResponseDto> actualResult = sut.findAll();

    assertAll(
        () -> assertFalse(actualResult.isEmpty()), () -> assertSame(expectedResult, actualResult));
    then(itemRepository).should(times(1)).findAllProjectedBy();
  }

  @Test
  void getItemsByCategoryId() {
    final CategoryEntity categoryEntity = itemEntity.getCategory();
    final Long id = categoryEntity.getId();
    final List<ItemResponseDto> expectedResult = List.of(itemResponseDto);
    given(categoryService.findCategoryById(id)).willReturn(categoryEntity);
    given(itemRepository.findByCategoryOrderByName(categoryEntity)).willReturn(expectedResult);

    final List<ItemResponseDto> actualResult = sut.getItemsByCategoryId(id);

    assertAll(
        () -> assertFalse(actualResult.isEmpty()),
        () ->
            assertEquals(
                expectedResult.get(0).getCategory().getId(),
                actualResult.get(0).getCategory().getId()),
        () ->
            assertEquals(
                expectedResult.get(0).getCategory().getName(),
                actualResult.get(0).getCategory().getName()));
    final InOrder inOrder = inOrder(categoryService, itemRepository);
    then(categoryService).should(inOrder, times(1)).findCategoryById(id);
    then(itemRepository).should(inOrder, times(1)).findByCategoryOrderByName(categoryEntity);
  }

  @Test
  void create() {
    final ItemResponseDto expectedResult = itemResponseDto;
    given(itemRepository.save(isA(ItemEntity.class))).willReturn(itemEntity);
    given(projectionFactory.createProjection(ItemResponseDto.class, itemEntity))
        .willReturn(expectedResult);

    final ItemResponseDto actualResult = sut.create(itemRequestDto);

    final InOrder inOrder = inOrder(itemRepository, projectionFactory);
    itemAsserts(expectedResult, actualResult);
    then(itemRepository).should(inOrder, times(1)).save(isA(ItemEntity.class));
    then(projectionFactory)
        .should(inOrder, times(1))
        .createProjection(ItemResponseDto.class, itemEntity);
  }

  @Test
  void update() {
    final ItemResponseDto expectedResult = itemResponseDto;
    given(itemRepository.findByCode(EXISTING_ITEM_CODE)).willReturn(Optional.of(itemEntity));
    given(itemRepository.save(isA(ItemEntity.class))).willReturn(itemEntity);
    given(projectionFactory.createProjection(ItemResponseDto.class, itemEntity))
        .willReturn(expectedResult);

    final ItemResponseDto actualResult = sut.update(EXISTING_ITEM_CODE, itemRequestDto);

    final InOrder inOrder = inOrder(itemRepository, projectionFactory);
    itemAsserts(expectedResult, actualResult);
    then(itemRepository).should(inOrder, times(1)).findByCode(EXISTING_ITEM_CODE);
    then(itemRepository).should(inOrder, times(1)).save(isA(ItemEntity.class));
    then(projectionFactory)
        .should(inOrder, times(1))
        .createProjection(ItemResponseDto.class, itemEntity);
    then(mapper).should(times(2)).map(any(), any());
  }

  @Test
  void delete() {
    itemEntity.setItemImageLink(UUID.randomUUID().toString());
    given(itemRepository.findByCode(EXISTING_ITEM_CODE)).willReturn(Optional.of(itemEntity));
    willDoNothing().given(fileStoreService).deleteFilesFromS3Bucket(itemEntity);

    sut.delete(EXISTING_ITEM_CODE);

    final InOrder inOrder = inOrder(itemRepository, fileStoreService);
    then(itemRepository).should(inOrder, times(1)).findByCode(EXISTING_ITEM_CODE);
    then(fileStoreService).should(inOrder, times(1)).deleteFilesFromS3Bucket(itemEntity);
    then(itemRepository).should(inOrder, times(1)).delete(itemEntity);
  }

  @Test
  void uploadItemImage() {
    final MultipartFile multipartFile =
        new MockMultipartFile("file", "image.png", "png", "test data".getBytes());
    final String itemImageUrl =
        String.format("https://%s-%s", UUID.randomUUID(), itemEntity.getName().replace(" ", "-"));
    given(itemRepository.findByCode(EXISTING_ITEM_CODE)).willReturn(Optional.of(itemEntity));
    given(fileStoreService.save(itemEntity, multipartFile)).willReturn(itemImageUrl);
    given(itemRepository.save(itemEntity)).willReturn(itemEntity);

    sut.uploadItemImage(EXISTING_ITEM_CODE, multipartFile);

    assertEquals(itemImageUrl, itemEntity.getItemImageLink());
  }

  @Test
  void uploadItemBlankImage() {
    given(itemRepository.findByCode(EXISTING_ITEM_CODE)).willReturn(Optional.of(itemEntity));
    sut.uploadItemImage(EXISTING_ITEM_CODE, null);
    then(fileStoreService).shouldHaveNoInteractions();
  }

  @Test
  void findItemByCode() {
    given(itemRepository.findByCode(EXISTING_ITEM_CODE)).willReturn(Optional.of(itemEntity));
    given(projectionFactory.createProjection(ItemResponseDto.class, itemEntity))
        .willReturn(itemResponseDto);

    final ItemResponseDto actualResult = sut.findItemByCode(EXISTING_ITEM_CODE);

    itemAsserts(itemResponseDto, actualResult);
    then(itemRepository).should(times(1)).findByCode(EXISTING_ITEM_CODE);
    then(projectionFactory).should(times(1)).createProjection(ItemResponseDto.class, itemEntity);
  }

  @Test
  void findItemByCodeThrowsNotFoundException() {
    final String NON_EXISTING_ITEM_CODE = UUID.randomUUID().toString();
    given(itemRepository.findByCode(NON_EXISTING_ITEM_CODE))
        .willThrow(new NotFoundException("item.error.code.not.found"));

    final NotFoundException actualResult =
        assertThrows(NotFoundException.class, () -> sut.findItemByCode(NON_EXISTING_ITEM_CODE));

    assertAll(
        () -> assertEquals(HttpStatus.NOT_FOUND, actualResult.getStatus()),
        () -> assertEquals("item.error.code.not.found", actualResult.getMessage()));
    then(itemRepository).should(times(1)).findByCode(NON_EXISTING_ITEM_CODE);
    then(projectionFactory).shouldHaveNoInteractions();
  }

  private static void itemAsserts(
      final ItemResponseDto expectedResult, final ItemResponseDto actualResult) {
    assertAll(
        () -> assertEquals(expectedResult.getCode(), actualResult.getCode()),
        () ->
            assertEquals(
                expectedResult.getCategory().getName(), actualResult.getCategory().getName()),
        () -> assertEquals(expectedResult.getBrand().getName(), actualResult.getBrand().getName()),
        () -> assertEquals(expectedResult.getName(), actualResult.getName()),
        () -> assertEquals(expectedResult.getPrice(), actualResult.getPrice()));
  }
}
