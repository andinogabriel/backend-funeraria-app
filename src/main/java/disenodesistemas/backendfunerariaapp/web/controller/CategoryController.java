package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.category.CategoryCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.category.CategoryQueryUseCase;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.CategoryResponseDto;
import jakarta.validation.Valid;
import java.util.List;
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

  private final CategoryCommandUseCase categoryCommandUseCase;
  private final CategoryQueryUseCase categoryQueryUseCase;

  @GetMapping
  public ResponseEntity<List<CategoryResponseDto>> findAll() {
    return ResponseEntity.ok(categoryQueryUseCase.findAll());
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity<CategoryResponseDto> findById(@PathVariable final Long id) {
    return ResponseEntity.ok(categoryQueryUseCase.findById(id));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<CategoryResponseDto> create(
      @RequestBody @Valid final CategoryRequestDto categoryRequestDto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(categoryCommandUseCase.create(categoryRequestDto));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<CategoryResponseDto> update(
      @PathVariable final Long id,
      @RequestBody @Valid final CategoryRequestDto categoryRequestDto) {
    return ResponseEntity.ok(categoryCommandUseCase.update(id, categoryRequestDto));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<OperationStatusModel> delete(@PathVariable final Long id) {
    categoryCommandUseCase.delete(id);
    return ResponseEntity.ok(
        OperationStatusModel.builder().name("DELETE CATEGORY").result("SUCCESSFUL").build());
  }
}
