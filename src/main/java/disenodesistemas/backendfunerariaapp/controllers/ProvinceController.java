package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.ProvinceDto;
import disenodesistemas.backendfunerariaapp.models.responses.ProvinceRest;
import disenodesistemas.backendfunerariaapp.service.ProvinceService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/v1/provinces")
public class ProvinceController {

    @Autowired
    ProvinceService provinceService;

    @Autowired
    ModelMapper mapper;

    @GetMapping
    public List<ProvinceRest> getAllProvinces() {
        List<ProvinceDto> provincesDto = provinceService.getAllProvinces();
        List<ProvinceRest> provincesRest = new ArrayList<>();
        for (ProvinceDto province : provincesDto) {
            ProvinceRest provinceRest = mapper.map(province, ProvinceRest.class);
            provincesRest.add(provinceRest);
        }
        return provincesRest;
    }

}
