package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.service.CategoryService;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.projection.ProjectionFactory;
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
  private final ProjectionFactory projectionFactory;

  @GetMapping
  public List<CategoryResponseDto> getAllCategories() {
    return categoryService.findAll();
  }

  @GetMapping(path = "/{id}")
  public CategoryResponseDto getCategoryById(@PathVariable final Long id) {
    return projectionFactory.createProjection(
        CategoryResponseDto.class, categoryService.findCategoryById(id));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public CategoryResponseDto createCategory(
      @RequestBody @Valid final CategoryRequestDto categoryRequestDto) {
    return categoryService.create(categoryRequestDto);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public CategoryResponseDto updateCategory(
      @PathVariable final Long id,
      @RequestBody @Valid final CategoryRequestDto categoryRequestDto) {
    return categoryService.update(id, categoryRequestDto);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public OperationStatusModel deleteCategory(@PathVariable final Long id) {
    categoryService.delete(id);
    return OperationStatusModel.builder().name("DELETE").result("SUCCESS").build();
  }
}
