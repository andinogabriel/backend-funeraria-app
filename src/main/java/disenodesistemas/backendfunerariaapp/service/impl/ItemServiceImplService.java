package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.ItemResponseDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import disenodesistemas.backendfunerariaapp.service.CategoryService;
import disenodesistemas.backendfunerariaapp.service.FileStoreService;
import disenodesistemas.backendfunerariaapp.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.nonNull;


@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImplService implements ItemService {

    private final ItemRepository itemRepository;
    private final CategoryService categoryService;
    private final FileStoreService fileStoreService;
    private final ProjectionFactory projectionFactory;
    private final ModelMapper mapper;

    @Override
    public List<ItemResponseDto> getAllItems() {
        return itemRepository.findAllProjectedBy();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponseDto> getItemsByCategoryId(final Long id) {
        return itemRepository.findByCategoryOrderByName(categoryService.findCategoryById(id));
    }

    @Override
    @Transactional
    public ItemResponseDto createItem(final ItemRequestDto itemRequestDto) {
        val itemEntity = ItemEntity.builder()
                .brand(mapper.map(itemRequestDto.getBrand(), BrandEntity.class))
                .category(mapper.map(itemRequestDto.getCategory(), CategoryEntity.class))
                .code(UUID.randomUUID().toString())
                .description(itemRequestDto.getDescription())
                .name(itemRequestDto.getName())
                .price(itemRequestDto.getPrice())
                .itemHeight(itemRequestDto.getItemHeight())
                .itemLength(itemRequestDto.getItemLength())
                .itemHeight(itemRequestDto.getItemHeight())
                .build();
        return projectionFactory.createProjection(ItemResponseDto.class, itemRepository.save(itemRepository.save(itemEntity)));
    }

    @Override
    @Transactional
    public ItemResponseDto updateItem(final String code, final ItemRequestDto itemRequestDto) {
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
        return projectionFactory.createProjection(ItemResponseDto.class, itemRepository.save(itemEntity));
    }

    @Transactional
    public void deleteItem(final String code) {
        val itemEntity = getItemByCode(code);
        if(nonNull(itemEntity.getItemImageLink()))
            fileStoreService.deleteFilesFromS3Bucket(itemEntity);
        itemRepository.delete(itemEntity);
    }


    @Override
    @Transactional
    public void uploadItemImage(final String code, final MultipartFile image) {
        val itemEntity = getItemByCode(code);
        if(nonNull(image))
            itemEntity.setItemImageLink(fileStoreService.save(itemEntity, image));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponseDto findItemByCode(final String code) {
        return itemRepository.findByCode(code)
                .map(itemEntity -> projectionFactory.createProjection(ItemResponseDto.class, itemEntity))
                .orElseThrow(() -> new AppException("item.error.code.not.found", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public ItemEntity getItemByCode(final String code) {
        return itemRepository.findByCode(code).orElseThrow(() -> new AppException("item.error.code.not.found", HttpStatus.NOT_FOUND));
    }

}
