package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.BrandDto;
import disenodesistemas.backendfunerariaapp.dto.ItemDto;
import disenodesistemas.backendfunerariaapp.models.requests.BrandCreateRequestModel;
import disenodesistemas.backendfunerariaapp.models.responses.BrandRest;
import disenodesistemas.backendfunerariaapp.models.responses.ItemRest;
import disenodesistemas.backendfunerariaapp.models.responses.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.BrandService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/v1/brands")
public class BrandController {

    @Autowired
    BrandService brandService;

    @Autowired
    ModelMapper mapper;

    @GetMapping
    public List<BrandRest> getAllBrandes() {
        List<BrandDto> brandesDto = brandService.getAllBrands();
        List<BrandRest> brandesRest = new ArrayList<>();
        brandesDto.forEach(brandDto -> {
            BrandRest brandRest = mapper.map(brandDto, BrandRest.class);
            brandesRest.add(brandRest);
        });
        return brandesRest;
    }

    @GetMapping(path = "/{id}")
    public BrandRest getBrandById(@PathVariable long id) {
        BrandDto brandDto = brandService.getBrandById(id);
        return mapper.map(brandDto, BrandRest.class);
    }

    @GetMapping(path = "/paginated")
    public Page<BrandRest> getBrandsPaginated(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value="limit", defaultValue = "5") int limit, @RequestParam(value = "sortBy", defaultValue = "name") String sortBy, @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        Page<BrandDto> brandsDto = brandService.getBrandsPaginated(page, limit, sortBy, sortDir);
        return mapper.map(brandsDto, Page.class);
    }

    @GetMapping(path = "/search")
    public Page<BrandRest> getBrandsByName(@RequestParam(value = "name") String name, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value="limit", defaultValue = "10") int limit, @RequestParam(value = "sortBy", defaultValue = "name") String sortBy, @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        Page<BrandDto> brandsDto = brandService.getBrandsByName(name, page, limit, sortBy, sortDir);
        return mapper.map(brandsDto, Page.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public BrandRest createBrand(@RequestBody @Valid BrandCreateRequestModel brandCreateRequestModel) {
        BrandDto brandDto = mapper.map(brandCreateRequestModel, BrandDto.class);
        BrandDto createdBrand = brandService.createBrand(brandDto);
        return mapper.map(createdBrand, BrandRest.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}")
    public BrandRest updateBrand(@PathVariable long id, @RequestBody @Valid BrandCreateRequestModel brandCreateRequestModel) {
        BrandDto brandDto = mapper.map(brandCreateRequestModel, BrandDto.class);
        BrandDto updatedBrand = brandService.updateBrand(id, brandDto);
        return mapper.map(updatedBrand, BrandRest.class);
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
