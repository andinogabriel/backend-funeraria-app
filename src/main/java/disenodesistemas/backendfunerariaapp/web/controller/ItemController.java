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
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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

  /**
   * Server-side paginated read for the items list page. Mirrors the contract used by the
   * affiliates / incomes paginated endpoints: 0-indexed page, configurable sort, every
   * filter param optional. Defaults to {@code sortBy=name asc} so the operator lands on
   * an alphabetised page when no preference is persisted.
   */
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @GetMapping("/paginated")
  public Page<ItemResponseDto> getItemsPaginated(
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "limit", defaultValue = "10") final int limit,
      @RequestParam(value = "sortBy", defaultValue = "name") final String sortBy,
      @RequestParam(value = "sortDir", defaultValue = "asc") final String sortDir,
      @RequestParam(value = "code", required = false) final String code,
      @RequestParam(value = "name", required = false) final String name,
      @RequestParam(value = "categoryName", required = false) final String categoryName,
      @RequestParam(value = "brandName", required = false) final String brandName) {
    return itemQueryUseCase.getItemsPaginated(
        page, limit, sortBy, sortDir, code, name, categoryName, brandName);
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

  /**
   * Admin-only papelera surface — filtered + paginated read of the soft-deleted items
   * ordered most-recent-first. Read-only by design: this endpoint ships no restore /
   * purge actions, the view is for compliance / audit consultation only (same shape
   * decided for the funeral / affiliate / plan papelera).
   *
   * <p>Spring's path matching gives literal segments priority over path variables, so
   * {@code GET /api/v1/items/deleted} lands here rather than on
   * {@code GET /api/v1/items/{code}}. No regex constraint is added on {@code {code}}
   * because existing items may carry non-UUID codes from earlier seed data.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/deleted")
  public Page<ItemResponseDto> findAllDeleted(
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "limit", defaultValue = "20") final int limit,
      @RequestParam(value = "code", required = false) final String code,
      @RequestParam(value = "name", required = false) final String name,
      @RequestParam(value = "categoryName", required = false) final String categoryName,
      @RequestParam(value = "brandName", required = false) final String brandName,
      @RequestParam(value = "deletedBy", required = false) final String deletedBy,
      @RequestParam(value = "deletedFrom", required = false) final Instant deletedFrom,
      @RequestParam(value = "deletedTo", required = false) final Instant deletedTo) {
    return itemQueryUseCase.findAllDeleted(
        page, limit, code, name, categoryName, brandName, deletedBy, deletedFrom, deletedTo);
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
