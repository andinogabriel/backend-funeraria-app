package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.BrandCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.Interface.IBrand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("api/v1/brands")
public class BrandController {

    private final IBrand brandService;
    private final ProjectionFactory projectionFactory;

    @Autowired
    public BrandController(IBrand brandService, ProjectionFactory projectionFactory) {
        this.brandService = brandService;
        this.projectionFactory = projectionFactory;
    }


    @GetMapping
    public List<BrandResponseDto> getAllBrandes() {
        return brandService.getAllBrands();
    }

    @GetMapping(path = "/{id}")
    public BrandResponseDto getBrandById(@PathVariable long id) {
        return projectionFactory.createProjection(
                BrandResponseDto.class,
                brandService.getBrandById(id)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public BrandResponseDto createBrand(@RequestBody @Valid BrandCreationDto brandCreationDto) {
        return brandService.createBrand(brandCreationDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}")
    public BrandResponseDto updateBrand(@PathVariable long id, @RequestBody @Valid BrandCreationDto brandCreationDto) {
        return brandService.updateBrand(id, brandCreationDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public OperationStatusModel deleteBrand(@PathVariable long id) {
        OperationStatusModel operationStatusModel = new OperationStatusModel();
        operationStatusModel.setName("DELETE");
        brandService.deleteBrand(id);
        operationStatusModel.setResult("SUCCESS");
        return operationStatusModel;
    }

}
