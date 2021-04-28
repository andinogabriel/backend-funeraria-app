package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.BrandDto;
import disenodesistemas.backendfunerariaapp.models.requests.BrandCreateRequestModel;
import disenodesistemas.backendfunerariaapp.models.responses.BrandRest;
import disenodesistemas.backendfunerariaapp.models.responses.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.BrandService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping
    public BrandRest createBrand(@RequestBody @Valid BrandCreateRequestModel brandCreateRequestModel) {
        BrandDto brandDto = mapper.map(brandCreateRequestModel, BrandDto.class);
        BrandDto createdBrand = brandService.createBrand(brandDto);
        BrandRest brandToReturn = mapper.map(createdBrand, BrandRest.class);
        return brandToReturn;
    }

    @PutMapping(path = "/{id}")
    public BrandRest updateBrand(@PathVariable long id, @RequestBody @Valid BrandCreateRequestModel brandCreateRequestModel) {
        BrandDto brandDto = mapper.map(brandCreateRequestModel, BrandDto.class);
        BrandDto updatedBrand = brandService.updateBrand(id, brandDto);
        BrandRest brandToReturn = mapper.map(updatedBrand, BrandRest.class);
        return brandToReturn;
    }

    @DeleteMapping(path = "/{id}")
    public OperationStatusModel deleteBrand(@PathVariable long id) {
        OperationStatusModel operationStatusModel = new OperationStatusModel();
        operationStatusModel.setName("DELETE");
        brandService.deleteBrand(id);
        operationStatusModel.setResult("SUCCESS");
        return operationStatusModel;
    }

}
