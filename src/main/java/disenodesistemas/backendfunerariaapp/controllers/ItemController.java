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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/items")
public class ItemController {

    @Autowired
    ItemService itemService;

    @Autowired
    ModelMapper mapper;

    @GetMapping
    public List<ItemRest> getAllItems() {
        List<ItemDto> itemsDto = itemService.getAllItems();
        List<ItemRest> itemsRest = new ArrayList<>();
        itemsDto.forEach(i -> itemsRest.add(mapper.map(i, ItemRest.class)));
        return itemsRest;
    }

    @GetMapping(path = "/paginated")
    public Page<ItemRest> getAllItemsPaginated(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value="limit", defaultValue = "5") int limit, @RequestParam(value = "sortBy", defaultValue = "name") String[] sortBy, @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        Page<ItemDto> itemsDto = itemService.getItemsPaginated(page, limit, sortBy, sortDir);
        return mapper.map(itemsDto, Page.class);
    }

    @GetMapping(path = "/{id}")
    public ItemRest getItemById(@PathVariable long id) {
        ItemDto itemDto = itemService.getItemById(id);
        return mapper.map(itemDto, ItemRest.class);
    }

    @GetMapping(path = "/search")
    public Page<ItemRest> getItemsByName(@RequestParam(value = "name") String name, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value="limit", defaultValue = "10") int limit, @RequestParam(value = "sortBy", defaultValue = "name") String sortBy, @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        Page<ItemDto> itemsDto = itemService.getItemsByName(name, page, limit, sortBy, sortDir);
        return mapper.map(itemsDto, Page.class);
    }

    @GetMapping(path = "/search/{id}")
    public Page<ItemRest> searchItemsByCategoryAndName(@PathVariable long id, @RequestParam(value = "name") String name, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value="limit", defaultValue = "10") int limit, @RequestParam(value = "sortBy", defaultValue = "name") String sortBy, @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        Page<ItemDto> itemsDto = itemService.getItemsByCategoryContaining(id, name, page, limit, sortBy, sortDir);
        return mapper.map(itemsDto, Page.class);
    }

    @GetMapping(path = "/category/{id}")
    public List<ItemRest> getItemsByCategoryId(@RequestParam(value = "categoryId") long categoryId) {
        List<ItemDto> itemsDto = itemService.getItemsByCategoryId(categoryId);
        List<ItemRest> itemsRest = new ArrayList<>();
        itemsDto.forEach(i -> itemsRest.add(mapper.map(i, ItemRest.class)));
        return itemsRest;
    }

    @GetMapping(path = "/category/paginated/{id}")
    public Page<ItemRest> getItemsPaginatedByCategoryId(@PathVariable long id, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value="limit", defaultValue = "10") int limit, @RequestParam(value = "sortBy", defaultValue = "name") String sortBy, @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        Page<ItemDto> itemsDto = itemService.getItemsPaginatedByCategoryId(id, page, limit, sortBy, sortDir);
        return mapper.map(itemsDto, Page.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ItemRest createItem(@RequestBody @Valid ItemRequestModel itemRequestModel) {
        ItemCreationDto itemDto = mapper.map(itemRequestModel, ItemCreationDto.class);
        ItemDto createdItem = itemService.createItem(itemDto);
        return mapper.map(createdItem, ItemRest.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}")
    public ItemRest updateItem(@PathVariable long id, @RequestBody @Valid ItemRequestModel itemRequestModel) {
        ItemCreationDto itemCreationDto = mapper.map(itemRequestModel, ItemCreationDto.class);
        ItemDto itemDto = itemService.updateItem(id, itemCreationDto);
        return mapper.map(itemDto, ItemRest.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public OperationStatusModel deleteItem(@PathVariable long id) {
        OperationStatusModel operationStatusModel = new OperationStatusModel();
        operationStatusModel.setName("DELETE");
        itemService.deleteItem(id);
        operationStatusModel.setName("SUCCESS");
        return operationStatusModel;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "{id}/image/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void uploadItemImage(@PathVariable("id") long id, @RequestParam("file")MultipartFile file) {
        itemService.uploadItemImage(id, file);
    }

    @GetMapping("{id}/image/download")
    public byte[] downloadItemImage(@PathVariable("id") long id) {
        return itemService.downloadItemImage(id);
    }

}
