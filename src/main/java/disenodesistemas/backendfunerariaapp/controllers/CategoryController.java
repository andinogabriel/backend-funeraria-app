package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.CategoryCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.Interface.ICategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("api/v1/categories")
public class CategoryController {

    private final ICategory categoryService;
    private final ProjectionFactory projectionFactory;

    @Autowired
    public CategoryController(ICategory categoryService, ProjectionFactory projectionFactory) {
        this.categoryService = categoryService;
        this.projectionFactory = projectionFactory;
    }


    @GetMapping
    public List<CategoryResponseDto> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping(path = "/{id}")
    public CategoryResponseDto getCategoryById(@PathVariable long id) {
        return projectionFactory.createProjection(CategoryResponseDto.class, categoryService.findCategoryById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public CategoryResponseDto createCategory(@RequestBody @Valid CategoryCreationDto categoryCreationDto) {
        return categoryService.createCategory(categoryCreationDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public CategoryResponseDto updateCategory(@PathVariable long id, @RequestBody @Valid CategoryCreationDto categoryCreationDto) {
        return categoryService.updateCategory(id, categoryCreationDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public OperationStatusModel deleteCategory(@PathVariable long id) {
        OperationStatusModel operationStatusModel = new OperationStatusModel();
        operationStatusModel.setName("DELETE");
        categoryService.deleteCategory(id);
        operationStatusModel.setName("SUCCESS");
        return operationStatusModel;
    }

}
