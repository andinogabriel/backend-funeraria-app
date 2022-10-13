package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.service.CityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/cities")
public class CityController {

    private final CityService cityService;

    public CityController(final CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping(path = "/{id}")
    public CityResponseDto getCityById(@PathVariable final Long id) {
        return cityService.getCityById(id);
    }

    @GetMapping
    public List<CityResponseDto> getCitiesByProvinceId(@RequestParam(value = "province_id") final Long id) {
        return cityService.getCitiesByProvinceId(id);
    }

}
