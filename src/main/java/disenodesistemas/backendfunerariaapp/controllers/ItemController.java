package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.utils.ApiConstants.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import disenodesistemas.backendfunerariaapp.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.ItemResponseDto;
import disenodesistemas.backendfunerariaapp.service.ItemService;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(VERSION + ITEMS)
@RequiredArgsConstructor
@Slf4j
public class ItemController {

  private final ItemService itemService;

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @GetMapping
  public ResponseEntity<List<ItemResponseDto>> findAll() {
    return ResponseEntity.ok(itemService.findAll());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping(path = "/{code}")
  public ResponseEntity<ItemResponseDto> findById(@PathVariable final String code) {
    return ResponseEntity.ok(itemService.findById(code));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @GetMapping(path = "/category/{id}")
  public ResponseEntity<List<ItemResponseDto>> findItemsByCategoryId(@PathVariable final Long id) {
    return ResponseEntity.ok(itemService.getItemsByCategoryId(id));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<ItemResponseDto> create(
      @RequestBody @Valid final ItemRequestDto itemRequestModel) {
    return ResponseEntity.status(HttpStatus.CREATED).body(itemService.create(itemRequestModel));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(path = "/{code}")
  public ResponseEntity<ItemResponseDto> update(
      @PathVariable(name = "code") final String code,
      @RequestBody @Valid final ItemRequestDto itemRequestModel) {
    return ResponseEntity.ok(itemService.update(code, itemRequestModel));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping(path = "/{code}")
  public ResponseEntity<OperationStatusModel> delete(@PathVariable final String code) {
    itemService.delete(code);
    return ResponseEntity.ok(
        OperationStatusModel.builder().name("DELETE ITEM").result("SUCCESSFUL").build());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping(
      path = "/{code}/image/upload",
      consumes = MULTIPART_FORM_DATA_VALUE,
      produces = APPLICATION_JSON_VALUE)
  public void uploadItemImage(
      @PathVariable final String code, @RequestParam("file") final MultipartFile file) {
    itemService.uploadItemImage(code, file);
  }
}
