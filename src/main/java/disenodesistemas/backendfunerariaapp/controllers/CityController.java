package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.CityDto;
import disenodesistemas.backendfunerariaapp.models.responses.CityRest;
import disenodesistemas.backendfunerariaapp.service.CityService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/v1/cities")
public class CityController {

    @Autowired
    CityService cityService;

    @Autowired
    ModelMapper mapper;

    @GetMapping(path = "/{id}")
    public CityRest getCityById(@PathVariable long id) {
        CityDto cityDto = cityService.getCityById(id);
        return mapper.map(cityDto, CityRest.class);
    }

    @GetMapping
    public List<CityRest> getCitiesByProvinceId(@RequestParam(value = "province_id") long id) {
        List<CityDto> citiesDto = cityService.getCitiesByProvinceId(id);
        List<CityRest> citiesRest = new ArrayList<>();
        for (CityDto city : citiesDto) {
            CityRest cityRest = mapper.map(city, CityRest.class);
            citiesRest.add(cityRest);
        }
        return citiesRest;
    }

}
