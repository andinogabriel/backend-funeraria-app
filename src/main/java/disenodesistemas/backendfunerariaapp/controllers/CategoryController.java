package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.CategoryDto;
import disenodesistemas.backendfunerariaapp.models.requests.CategoryCreateRequestModel;
import disenodesistemas.backendfunerariaapp.models.responses.CategoryRest;
import disenodesistemas.backendfunerariaapp.models.responses.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/v1/categories")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    ModelMapper mapper;

    @GetMapping
    public List<CategoryRest> getAllCategories() {
        List<CategoryDto> categoriesDto = categoryService.getAllCategories();
        List<CategoryRest> categoriesRest = new ArrayList<>();
        categoriesDto.forEach(category -> {
            CategoryRest categoryRest = mapper.map(category, CategoryRest.class);
            categoriesRest.add(categoryRest);
        });
        return categoriesRest;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public CategoryRest createCategory(@RequestBody @Valid CategoryCreateRequestModel categoryCreateRequestModel) {
        CategoryDto categoryDto = mapper.map(categoryCreateRequestModel, CategoryDto.class);
        CategoryDto createdCategory = categoryService.createCategory(categoryDto);
        return mapper.map(createdCategory, CategoryRest.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public CategoryRest updateCategory(@PathVariable long id, @RequestBody @Valid CategoryCreateRequestModel categoryCreateRequestModel) {
        CategoryDto categoryDto = mapper.map(categoryCreateRequestModel, CategoryDto.class);
        CategoryDto updatedCategory = categoryService.updateCategory(id, categoryDto);
        return mapper.map(updatedCategory, CategoryRest.class);
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
