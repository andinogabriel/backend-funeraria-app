package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.ItemCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.ItemResponseDto;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.Interface.IItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("api/v1/items")
public class ItemController {

    private final IItem itemService;
    private final ProjectionFactory projectionFactory;

    @Autowired
    public ItemController(IItem itemService, ProjectionFactory projectionFactory) {
        this.itemService = itemService;
        this.projectionFactory = projectionFactory;
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping
    public List<ItemResponseDto> getAllItems() {
        return itemService.getAllItems();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{id}")
    public ItemResponseDto getItemById(@PathVariable Long id) {
        return projectionFactory.createProjection(ItemResponseDto.class, itemService.getItemById(id));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(path = "/category/{id}")
    public List<ItemResponseDto> getItemsByCategoryId(@PathVariable Long id) {
        return itemService.getItemsByCategoryId(id);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ItemResponseDto createItem(@RequestBody @Valid ItemCreationDto itemCreationDto) {
        return itemService.createItem(itemCreationDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}")
    public ItemResponseDto updateItem(@PathVariable Long id, @RequestBody @Valid ItemCreationDto itemRequestModel) {
        return itemService.updateItem(id, itemRequestModel);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public OperationStatusModel deleteItem(@PathVariable Long id) {
        OperationStatusModel operationStatusModel = new OperationStatusModel();
        operationStatusModel.setName("DELETE");
        itemService.deleteItem(id);
        operationStatusModel.setName("SUCCESS");
        return operationStatusModel;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "{id}/image/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void uploadItemImage(@PathVariable("id") Long id, @RequestParam("file")MultipartFile file) {
        itemService.uploadItemImage(id, file);
    }


}
