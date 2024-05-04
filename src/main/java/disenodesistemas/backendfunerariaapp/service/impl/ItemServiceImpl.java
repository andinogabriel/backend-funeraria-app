package disenodesistemas.backendfunerariaapp.service.impl;

import static java.util.Objects.nonNull;

import disenodesistemas.backendfunerariaapp.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.ItemResponseDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import disenodesistemas.backendfunerariaapp.service.CategoryService;
import disenodesistemas.backendfunerariaapp.service.FileStoreService;
import disenodesistemas.backendfunerariaapp.service.ItemService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

  private final ItemRepository itemRepository;
  private final CategoryService categoryService;
  private final FileStoreService fileStoreService;
  private final ProjectionFactory projectionFactory;
  private final ModelMapper mapper;

  @Override
  public List<ItemResponseDto> findAll() {
    return itemRepository.findAllProjectedBy();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ItemResponseDto> getItemsByCategoryId(final Long id) {
    return itemRepository.findByCategoryOrderByName(categoryService.findCategoryById(id));
  }

  @Override
  @Transactional
  public ItemResponseDto create(final ItemRequestDto itemRequestDto) {
    val itemEntity =
        ItemEntity.builder()
            .brand(mapper.map(itemRequestDto.getBrand(), BrandEntity.class))
            .category(mapper.map(itemRequestDto.getCategory(), CategoryEntity.class))
            .code(UUID.randomUUID().toString())
            .description(itemRequestDto.getDescription())
            .name(itemRequestDto.getName())
            .price(itemRequestDto.getPrice())
            .itemHeight(itemRequestDto.getItemHeight())
            .itemLength(itemRequestDto.getItemLength())
            .itemWidth(itemRequestDto.getItemWidth())
            .build();
    return projectionFactory.createProjection(
        ItemResponseDto.class, itemRepository.save(itemEntity));
  }

  @Override
  @Transactional
  public ItemResponseDto update(final String code, final ItemRequestDto itemRequestDto) {
    val itemEntity = getItemByCode(code);
    itemEntity.setCategory(mapper.map(itemRequestDto.getCategory(), CategoryEntity.class));
    itemEntity.setBrand(mapper.map(itemRequestDto.getBrand(), BrandEntity.class));
    itemEntity.setName(itemRequestDto.getName());
    itemEntity.setPrice(itemRequestDto.getPrice());
    itemEntity.setCode(itemRequestDto.getCode());
    itemEntity.setDescription(itemRequestDto.getDescription());
    itemEntity.setItemLength(itemRequestDto.getItemLength());
    itemEntity.setItemHeight(itemRequestDto.getItemHeight());
    itemEntity.setItemWidth(itemRequestDto.getItemWidth());
    return projectionFactory.createProjection(
        ItemResponseDto.class, itemRepository.save(itemEntity));
  }

  @Transactional
  public void delete(final String code) {
    val itemEntity = getItemByCode(code);
    if (nonNull(itemEntity.getItemImageLink()))
      fileStoreService.deleteFilesFromS3Bucket(itemEntity);
    itemRepository.delete(itemEntity);
  }

  @Override
  @Transactional
  public void uploadItemImage(final String code, final MultipartFile image) {
    val itemEntity = getItemByCode(code);
    Optional.ofNullable(image)
        .ifPresentOrElse(
            imageToUpload -> {
              itemEntity.setItemImageLink(fileStoreService.save(itemEntity, image));
              itemRepository.save(itemEntity);
            },
            () -> log.info("archivo invalido para imagen"));
  }

  @Override
  @Transactional(readOnly = true)
  public ItemResponseDto findItemByCode(final String code) {
    return projectionFactory.createProjection(ItemResponseDto.class, getItemByCode(code));
  }

  private ItemEntity getItemByCode(final String code) {
    return itemRepository
        .findByCode(code)
        .orElseThrow(() -> new NotFoundException("item.error.code.not.found"));
  }
}
