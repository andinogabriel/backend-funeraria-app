package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.buckets.BucketName;
import disenodesistemas.backendfunerariaapp.dto.ItemCreationDto;
import disenodesistemas.backendfunerariaapp.dto.ItemDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.repository.BrandRepository;
import disenodesistemas.backendfunerariaapp.repository.CategoryRepository;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.apache.http.entity.ContentType.*;

@AllArgsConstructor
@Service
public class ItemService {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    FileStore fileStore;


    @Autowired
    ModelMapper mapper;

    public Page<ItemDto> getItemsPaginated(int page, int limit, String[] sortBy, String sortDir) {
        if (page > 0) {
            page = page - 1;
        }

        Pageable pageable = PageRequest.of(
                page, limit,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending()
        );

        Page<ItemEntity> itemEntities = itemRepository.findAll(pageable);
        return mapper.map(itemEntities, Page.class);
    }

    public List<ItemDto> getItemsByCategoryId(long id) {
        CategoryEntity categoryEntity = categoryRepository.findById(id);
        List<ItemEntity> itemEntities = itemRepository.findByCategoryOrderByName(categoryEntity);
        List<ItemDto> itemsDto = new ArrayList<>();
        itemEntities.forEach(i -> itemsDto.add(mapper.map(i, ItemDto.class)));
        return itemsDto;
    }


    public Page<ItemDto> getItemsPaginatedByCategoryId(long id, int page, int limit, String sortBy, String sortDir) {
        if (page > 0) {
            page = page - 1;
        }

        CategoryEntity categoryEntity = categoryRepository.findById(id);

        Pageable pageable = PageRequest.of(
                page, limit,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending()
        );

        Page<ItemEntity> itemEntities = itemRepository.findByCategory(pageable, categoryEntity);
        return mapper.map(itemEntities, Page.class);
    }



    public ItemDto createItem(ItemCreationDto itemCreationDto) {
        ItemEntity itemEntity = new ItemEntity();
        CategoryEntity categoryEntity = categoryRepository.findById(itemCreationDto.getCategory());
        BrandEntity brandEntity = brandRepository.findById(itemCreationDto.getBrand());

        itemEntity.setName(itemCreationDto.getName());
        itemEntity.setCategory(categoryEntity);
        UUID itemCode = UUID.randomUUID();
        itemEntity.setCode(itemCode.toString());
        itemEntity.setDescription(itemCreationDto.getDescription());
        itemEntity.setPrice(itemCreationDto.getPrice());
        itemEntity.setBrand(brandEntity);
        itemEntity.setItemLength(itemCreationDto.getItemLength());
        itemEntity.setItemHeight(itemCreationDto.getItemHeight());
        itemEntity.setItemWidth(itemCreationDto.getItemWidth());

        ItemEntity createdItem = itemRepository.save(itemEntity);
        return mapper.map(createdItem, ItemDto.class);
    }

    public ItemDto updateItem(long id, ItemCreationDto itemCreationDto) {

        ItemEntity itemEntity = itemRepository.findById(id);
        CategoryEntity categoryEntity = categoryRepository.findById(itemCreationDto.getCategory());
        BrandEntity brandEntity = brandRepository.findById(itemCreationDto.getBrand());

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
        return mapper.map(updatedItem, ItemDto.class);
    }

    public void deleteItem(long id) {
        ItemEntity itemEntity = itemRepository.findById(id);
        itemRepository.delete(itemEntity);
    }

    public ItemDto getItemById(long id) {
        ItemEntity itemEntity = itemRepository.findById(id);
        return mapper.map(itemEntity, ItemDto.class);
    }

    public Page<ItemDto> getItemsByName(String name, int page, int limit, String sortBy, String sortDir) {
        if (page > 0) {
            page = page - 1;
        }

        Pageable pageable = PageRequest.of(
                page, limit,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending()
        );

        Page<ItemEntity> itemEntities = itemRepository.findByNameContaining(pageable, name);
        return mapper.map(itemEntities, Page.class);
    }

    public Page<ItemDto> getItemsByCategoryContaining(long id, String name, int page, int limit, String sortBy, String sortDir) {
        //CategoryEntity categoryEntity = categoryRepository.findById(id);
        if (page > 0) {
            page = page - 1;
        }

        Pageable pageable = PageRequest.of(
                page, limit,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending()
        );
        Page<ItemEntity> items = itemRepository.findByCategoryAndNameContaining(pageable, id, name);
        return mapper.map(items, Page.class);
    }

    public void uploadItemImage(long id, MultipartFile file) {
        //1. Chequear si la imagen no esta vacia
        fileIsEmpty(file);

        //2. Si el archivo NO es una imagen valida
        isAnImage(file);

        //3. Si el item existe en la DB
        ItemEntity item = getItemOrThrowError(id);

        //4. Grabar metadata del archivo
        Map<String, String> metadata = exctractMetadata(file);

        //5. Almacenar la imagen en s3 y actualizar la db (itemImageLink) con el link de la imagen en s3
        //El nombre del actual bucket, luego el nombre de la carpeta con el nombre del item
        String path = String.format("%s/%s", BucketName.ITEM_IMAGE.getBucketName(), item.getName());
        String filename = String.format("%s-%s", file.getOriginalFilename(), UUID.randomUUID());

        try {
            //El cuarto parametro es actual archivo
            fileStore.save(path, filename, Optional.of(metadata), file.getInputStream());
            item.setItemImageLink(filename); //Seteo el nuevo link de la imagen
            itemRepository.save(item);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public byte[] downloadItemImage(long id) {
        ItemEntity itemEntity = getItemOrThrowError(id);
        ItemDto item = mapper.map(itemEntity, ItemDto.class);

        String path = String.format("%s/%s", BucketName.ITEM_IMAGE.getBucketName(), item.getName());

        return fileStore.download(path, item.getItemImageLink());
    }

    private Map<String, String> exctractMetadata(MultipartFile file) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
        return metadata;
    }

    private ItemEntity getItemOrThrowError(long id) {
        try {
            ItemDto itemDto = getItemById(id);
            return mapper.map(itemDto, ItemEntity.class);
        } catch (IllegalStateException ex) {
            throw new IllegalStateException(String.format("Articulo con ID: %s no encontrado.", id));
        }
    }

    private void isAnImage(MultipartFile file) {
        if(!Arrays.asList(IMAGE_JPEG.getMimeType(), IMAGE_PNG.getMimeType(), IMAGE_GIF.getMimeType()).contains(file.getContentType())) {
            throw new IllegalStateException("El archivo debe ser una imagen valida (JPEG, JPG, PNG, GIF).");
        }
    }

    private void fileIsEmpty(MultipartFile file) {
        if(file.isEmpty()) {
            throw new IllegalStateException("No se puede subir un archivo vacio [" + file.getSize() + "]");
        }
    }


}
