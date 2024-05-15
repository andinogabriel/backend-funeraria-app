package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.service.CategoryService;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  public ResponseEntity<List<CategoryResponseDto>> findAll() {
    return ResponseEntity.ok(categoryService.findAll());
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity<CategoryResponseDto> findById(@PathVariable final Long id) {
    return ResponseEntity.ok(categoryService.findById(id));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<CategoryResponseDto> create(
      @RequestBody @Valid final CategoryRequestDto categoryRequestDto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(categoryService.create(categoryRequestDto));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<CategoryResponseDto> update(
      @PathVariable final Long id,
      @RequestBody @Valid final CategoryRequestDto categoryRequestDto) {
    return ResponseEntity.ok(categoryService.update(id, categoryRequestDto));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<OperationStatusModel> delete(@PathVariable final Long id) {
    categoryService.delete(id);
    return ResponseEntity.ok(
        OperationStatusModel.builder().name("DELETE CATEGORY").result("SUCCESSFUL").build());
  }
}
