package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.response.ProvinceResponseDto;
import disenodesistemas.backendfunerariaapp.service.Interface.ProvinceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/provinces")
public class ProvinceController {

    private final ProvinceService provinceService;

    public ProvinceController(final ProvinceService provinceService) {
        this.provinceService = provinceService;
    }

    @GetMapping
    public List<ProvinceResponseDto> getAllProvinces() {
        return provinceService.getAllProvinces();
    }

}
