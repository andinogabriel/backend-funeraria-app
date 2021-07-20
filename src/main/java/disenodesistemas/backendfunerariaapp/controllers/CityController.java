package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.service.Interface.ICity;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/cities")
@AllArgsConstructor
public class CityController {

    @Autowired
    private final ICity cityService;


    @GetMapping(path = "/{id}")
    public CityResponseDto getCityById(@PathVariable Long id) {
        return cityService.getCityById(id);
    }

    @GetMapping
    public List<CityResponseDto> getCitiesByProvinceId(@RequestParam(value = "province_id") long id) {
        return cityService.getCitiesByProvinceId(id);
    }

}
