package disenodesistemas.backendfunerariaapp.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import disenodesistemas.backendfunerariaapp.application.model.FilePayload;
import disenodesistemas.backendfunerariaapp.application.usecase.item.ItemCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.item.ItemQueryUseCase;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ItemResponseDto;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
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
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

  private final ItemCommandUseCase itemCommandUseCase;
  private final ItemQueryUseCase itemQueryUseCase;

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @GetMapping
  public ResponseEntity<List<ItemResponseDto>> findAll() {
    return ResponseEntity.ok(itemQueryUseCase.findAll());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping(path = "/{code}")
  public ResponseEntity<ItemResponseDto> findById(@PathVariable final String code) {
    return ResponseEntity.ok(itemQueryUseCase.findById(code));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @GetMapping(path = "/category/{id}")
  public ResponseEntity<List<ItemResponseDto>> findItemsByCategoryId(@PathVariable final Long id) {
    return ResponseEntity.ok(itemQueryUseCase.getItemsByCategoryId(id));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<ItemResponseDto> create(
      @RequestBody @Valid final ItemRequestDto itemRequestModel) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(itemCommandUseCase.create(itemRequestModel));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(path = "/{code}")
  public ResponseEntity<ItemResponseDto> update(
      @PathVariable(name = "code") final String code,
      @RequestBody @Valid final ItemRequestDto itemRequestModel) {
    return ResponseEntity.ok(itemCommandUseCase.update(code, itemRequestModel));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping(path = "/{code}")
  public ResponseEntity<OperationStatusModel> delete(@PathVariable final String code) {
    itemCommandUseCase.delete(code);
    return ResponseEntity.ok(
        new OperationStatusModel("DELETE ITEM", "SUCCESSFUL"));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping(
      path = "/{code}/image/upload",
      consumes = MULTIPART_FORM_DATA_VALUE,
      produces = APPLICATION_JSON_VALUE)
  public void uploadItemImage(
      @PathVariable final String code, @RequestParam("file") final MultipartFile file) {
    try {
      itemCommandUseCase.uploadItemImage(
          code,
          new FilePayload(file.getOriginalFilename(), file.getContentType(), file.getBytes()));
    } catch (IOException ex) {
      log.atError()
          .setCause(ex)
          .addKeyValue("event", "item.image.upload.read_failed")
          .addKeyValue("code", code)
          .addKeyValue("filename", file.getOriginalFilename())
          .addKeyValue("contentType", file.getContentType())
          .log("item.image.upload.read_failed");
      throw new AppException("item.error.invalid.image.upload", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
