package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.ItemCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.ItemResponseDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.IBrand;
import disenodesistemas.backendfunerariaapp.service.Interface.ICategory;
import disenodesistemas.backendfunerariaapp.service.Interface.IFileStore;
import disenodesistemas.backendfunerariaapp.service.Interface.IItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.util.*;


@Service
public class ItemServiceImpl implements IItem {

    private final ItemRepository itemRepository;
    private final ICategory categoryService;
    private final IBrand brandService;
    private final IFileStore fileStoreService;
    private final ProjectionFactory projectionFactory;
    private final MessageSource messageSource;
    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, ICategory categoryService, IBrand brandService, IFileStore fileStoreService, ProjectionFactory projectionFactory, MessageSource messageSource) {
        this.itemRepository = itemRepository;
        this.categoryService = categoryService;
        this.brandService = brandService;
        this.fileStoreService = fileStoreService;
        this.projectionFactory = projectionFactory;
        this.messageSource = messageSource;
    }


    @Override
    public List<ItemResponseDto> getAllItems() {
        return itemRepository.findAllProjectedBy();
    }

    @Override
    public List<ItemResponseDto> getItemsByCategoryId(long id) {
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
        return projectionFactory.createProjection(ItemResponseDto.class, createdItem);
    }

    @Override
    public ItemResponseDto updateItem(long id, ItemCreationDto itemCreationDto) {

        ItemEntity itemEntity = getItemById(id);
        CategoryEntity categoryEntity = categoryService.findCategoryById(itemCreationDto.getCategory());
        BrandEntity brandEntity = brandService.getBrandById(itemCreationDto.getBrand());

        itemEntity.setName(itemCreationDto.getName());
        itemEntity.setPrice(itemCreationDto.getPrice());
        itemEntity.setCategory(categoryEntity);
        itemEntity.setCode(itemCreationDto.getCode());
        itemEntity.setDescription(itemCreationDto.getDescription());
        //itemEntity.setItemImageLink(itemCreationDto.getItemImageLink());
        itemEntity.setBrand(brandEntity);
        itemEntity.setItemLength(itemCreationDto.getItemLength());
        itemEntity.setItemHeight(itemCreationDto.getItemHeight());
        itemEntity.setItemWidth(itemCreationDto.getItemWidth());

        ItemEntity updatedItem = itemRepository.save(itemEntity);
        return projectionFactory.createProjection(ItemResponseDto.class, updatedItem);
    }

    public void deleteItem(long id) {
        ItemEntity itemEntity = getItemById(id);
        //Para eliminar las imagenes almacenadas en el s3 bucket le debo pasar el nombre del directorio donde estan las n imagenes
        //No es que tiene n images, sino que al poner otra imagen esta se almacena en el mismo directorio por ende al eliminar el articulo eliminamos todas las imagenes que tuvo
        fileStoreService.deleteFilesFromS3Bucket(itemEntity.getId() + "-" + itemEntity.getName().replaceAll("\\s", "-"));
        itemRepository.delete(itemEntity);
    }

    @Override
    public ItemEntity getItemById(long id) {
        return itemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(
                        messageSource.getMessage("item.error.not.found", null, Locale.getDefault())
                )
        );
    }

    @Override
    public void uploadItemImage(long id, MultipartFile file) {
        ItemEntity item = getItemById(id);
        // Almacenar la imagen en s3 y actualizar la db (itemImageLink) con el link de la imagen en s3
        //El campo imagen sera: el nombre del actual bucket/el id del articulo-nombre-del-articulo    <-- Asi quedara formado el directorio/folder del file
        String path = String.format("%s/%s", bucketName, item.getId() + "-" + item.getName().replaceAll("\\s", "-"));
        //El nombre de la imagen quedara formado por el nombre original-UUID random
        String filename = String.format("%s-%s", file.getOriginalFilename(), UUID.randomUUID());

        fileStoreService.save(path, filename, file);
        item.setItemImageLink(filename); //Seteo el nuevo link de la imagen
        itemRepository.save(item);
    }

}
