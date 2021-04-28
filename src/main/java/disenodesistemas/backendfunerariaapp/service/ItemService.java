package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.ItemCreationDto;
import disenodesistemas.backendfunerariaapp.dto.ItemDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.repository.BrandRepository;
import disenodesistemas.backendfunerariaapp.repository.CategoryRepository;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ItemService {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    ModelMapper mapper;

    public Page<ItemDto> getItemsPaginated(int page, int limit, String sortBy, String sortDir) {
        if (page > 0) {
            page = page - 1;
        }

        Pageable pageable = PageRequest.of(
                page, limit,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending()
        );

        Page<ItemEntity> itemEntities = itemRepository.findAllByOrderByName(pageable);
        Page<ItemDto> itemsDto = mapper.map(itemEntities, Page.class);
        return itemsDto;
    }

    public Page<ItemDto> getItemsByCategoryId(long id, int page, int limit, String sortBy, String sortDir) {
        CategoryEntity categoryEntity = categoryRepository.findById(id);

        Pageable pageable = PageRequest.of(
                page, limit,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending()
        );

        Page<ItemEntity> itemEntities = itemRepository.findByCategoryOrderByName(pageable, categoryEntity);
        Page<ItemDto> itemsDto = mapper.map(itemEntities, Page.class);
        return itemsDto;
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
        itemEntity.setImage(itemCreationDto.getImage());
        itemEntity.setPrice(itemCreationDto.getPrice());
        itemEntity.setBrand(brandEntity);
        itemEntity.setItemLength(itemCreationDto.getItemLength());
        itemEntity.setItemHeight(itemCreationDto.getItemHeight());
        itemEntity.setItemWidth(itemCreationDto.getItemWidth());

        ItemEntity createdItem = itemRepository.save(itemEntity);
        ItemDto itemDto = mapper.map(createdItem, ItemDto.class);
        return itemDto;
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
        itemEntity.setImage(itemCreationDto.getImage());
        itemEntity.setBrand(brandEntity);
        itemEntity.setItemLength(itemCreationDto.getItemLength());
        itemEntity.setItemHeight(itemCreationDto.getItemHeight());
        itemEntity.setItemWidth(itemCreationDto.getItemWidth());

        ItemEntity updatedItem = itemRepository.save(itemEntity);
        ItemDto itemDto = mapper.map(updatedItem, ItemDto.class);
        return itemDto;
    }

    public void deleteItem(long id) {
        ItemEntity itemEntity = itemRepository.findById(id);
        itemRepository.delete(itemEntity);
    }

    public ItemDto getItemById(long id) {
        ItemEntity itemEntity = itemRepository.findById(id);
        ItemDto itemDto = mapper.map(itemEntity, ItemDto.class);
        return itemDto;
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
        Page<ItemDto> itemsDto = mapper.map(itemEntities, Page.class);
        return itemsDto;
    }

}
