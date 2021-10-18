package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.ItemCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.ItemResponseDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.IBrand;
import disenodesistemas.backendfunerariaapp.service.Interface.ICategory;
import disenodesistemas.backendfunerariaapp.service.Interface.IFileStore;
import disenodesistemas.backendfunerariaapp.service.Interface.IItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements IItem {

    private final ItemRepository itemRepository;
    private final ICategory categoryService;
    private final IBrand brandService;
    private final IFileStore fileStoreService;
    private final ProjectionFactory projectionFactory;
    private final MessageSource messageSource;


    @Override
    public List<ItemResponseDto> getAllItems() {
        return itemRepository.findAllProjectedBy();
    }

    @Override
    public List<ItemResponseDto> getItemsByCategoryId(Long id) {
        CategoryEntity categoryEntity = categoryService.findCategoryById(id);
        return itemRepository.findByCategoryOrderByName(categoryEntity);
    }

    @Override
    public ItemResponseDto createItem(ItemCreationDto itemCreationDto) {
        CategoryEntity categoryEntity = categoryService.findCategoryById(itemCreationDto.getCategory());
        BrandEntity brandEntity = brandService.getBrandById(itemCreationDto.getBrand());

        ItemEntity itemEntity = ItemEntity.builder()
                .brand(brandEntity)
                .category(categoryEntity)
                .code(UUID.randomUUID().toString())
                .description(itemCreationDto.getDescription())
                .name(itemCreationDto.getName())
                .price(itemCreationDto.getPrice())
                .itemHeight(itemCreationDto.getItemHeight())
                .itemLength(itemCreationDto.getItemLength())
                .itemHeight(itemCreationDto.getItemHeight())
                .build();

        ItemEntity createdItem = itemRepository.save(itemEntity);
        return projectionFactory.createProjection(ItemResponseDto.class, itemRepository.save(createdItem));
    }

    @Override
    public ItemResponseDto updateItem(Long id, ItemCreationDto itemCreationDto) {

        ItemEntity itemEntity = getItemById(id);
        CategoryEntity categoryEntity = categoryService.findCategoryById(itemCreationDto.getCategory());
        BrandEntity brandEntity = brandService.getBrandById(itemCreationDto.getBrand());

        itemEntity.setName(itemCreationDto.getName());
        itemEntity.setPrice(itemCreationDto.getPrice());
        itemEntity.setCategory(categoryEntity);
        itemEntity.setCode(itemCreationDto.getCode());
        itemEntity.setDescription(itemCreationDto.getDescription());
        itemEntity.setBrand(brandEntity);
        itemEntity.setItemLength(itemCreationDto.getItemLength());
        itemEntity.setItemHeight(itemCreationDto.getItemHeight());
        itemEntity.setItemWidth(itemCreationDto.getItemWidth());

        ItemEntity updatedItem = itemRepository.save(itemEntity);
        return projectionFactory.createProjection(ItemResponseDto.class, updatedItem);
    }

    public void deleteItem(Long id) {
        ItemEntity itemEntity = getItemById(id);
        fileStoreService.deleteFilesFromS3Bucket(itemEntity);
        itemRepository.delete(itemEntity);
    }

    @Override
    public ItemEntity getItemById(Long id) {
        return itemRepository.findById(id).orElseThrow(
                () -> new AppException(
                        messageSource.getMessage("item.error.not.found", null, Locale.getDefault()),
                        HttpStatus.NOT_FOUND
                )
        );
    }

    @Override
    public void uploadItemImage(Long id, MultipartFile image) {
        ItemEntity itemEntity = getItemById(id);
        if(image != null)
            itemEntity.setItemImageLink(fileStoreService.save(itemEntity, image));
    }

}
