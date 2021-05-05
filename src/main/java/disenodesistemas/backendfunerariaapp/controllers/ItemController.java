package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.ItemCreationDto;
import disenodesistemas.backendfunerariaapp.dto.ItemDto;
import disenodesistemas.backendfunerariaapp.models.requests.ItemRequestModel;
import disenodesistemas.backendfunerariaapp.models.responses.ItemRest;
import disenodesistemas.backendfunerariaapp.models.responses.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.ItemService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/items")
public class ItemController {

    @Autowired
    ItemService itemService;

    @Autowired
    ModelMapper mapper;

    @GetMapping
    public Page<ItemRest> getAllItemsPaginated(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value="limit", defaultValue = "5") int limit, @RequestParam(value = "sortBy", defaultValue = "name") String sortBy, @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        Page<ItemDto> itemsDto = itemService.getItemsPaginated(page, limit, sortBy, sortDir);
        Page<ItemRest> itemsRest = mapper.map(itemsDto, Page.class);
        return itemsRest;
    }

    @GetMapping(path = "/{id}")
    public ItemRest getItemById(@PathVariable long id) {
        ItemDto itemDto = itemService.getItemById(id);
        ItemRest itemRest = mapper.map(itemDto, ItemRest.class);
        return itemRest;
    }

    @GetMapping(path = "/search")
    public Page<ItemRest> getItemsByName(@RequestParam(value = "name") String name, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value="limit", defaultValue = "10") int limit, @RequestParam(value = "sortBy", defaultValue = "name") String sortBy, @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        Page<ItemDto> itemsDto = itemService.getItemsByName(name, page, limit, sortBy, sortDir);
        Page<ItemRest> itemsRest = mapper.map(itemsDto, Page.class);
        return itemsRest;
    }

    @GetMapping(path = "/category/{id}")
    public Page<ItemRest> getItemsByCategoryId(@PathVariable long id, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value="limit", defaultValue = "10") int limit, @RequestParam(value = "sortBy", defaultValue = "name") String sortBy, @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        Page<ItemDto> itemsDto = itemService.getItemsByCategoryId(id, page, limit, sortBy, sortDir);
        Page<ItemRest> itemsRest = mapper.map(itemsDto, Page.class);
        return itemsRest;
    }

    @PostMapping
    public ItemRest createItem(@RequestBody @Valid ItemRequestModel itemRequestModel) {
        ItemCreationDto itemDto = mapper.map(itemRequestModel, ItemCreationDto.class);
        ItemDto createdItem = itemService.createItem(itemDto);
        ItemRest itemToReturn = mapper.map(createdItem, ItemRest.class);
        return itemToReturn;
    }

    @PutMapping(path = "/{id}")
    public ItemRest updateItem(@PathVariable long id, ItemRequestModel itemRequestModel) {
        ItemCreationDto itemCreationDto = mapper.map(itemRequestModel, ItemCreationDto.class);
        ItemDto itemDto = itemService.updateItem(id, itemCreationDto);
        ItemRest itemRest = mapper.map(itemDto, ItemRest.class);
        return itemRest;
    }

    @DeleteMapping(path = "/{id}")
    public OperationStatusModel deleteItem(@PathVariable long id) {
        OperationStatusModel operationStatusModel = new OperationStatusModel();
        operationStatusModel.setName("DELETE");
        itemService.deleteItem(id);
        operationStatusModel.setName("SUCCESS");
        return operationStatusModel;
    }

    @PostMapping(path = "{id}/image/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void uploadItemImage(@PathVariable("id") long id, @RequestParam("file")MultipartFile file) {
        itemService.uploadItemImage(id, file);
    }

    @GetMapping("{id}/image/download")
    public byte[] downloadItemImage(@PathVariable("id") long id) {
        return itemService.downloadItemImage(id);
    }

}
