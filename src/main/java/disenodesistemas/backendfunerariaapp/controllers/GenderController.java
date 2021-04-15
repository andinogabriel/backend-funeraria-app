package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.GenderDto;
import disenodesistemas.backendfunerariaapp.models.responses.GenderRest;
import disenodesistemas.backendfunerariaapp.service.GenderService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/v1/genders")
public class GenderController {

    @Autowired
    GenderService genderService;

    @Autowired
    ModelMapper mapper;

    @GetMapping
    public List<GenderRest> getGenders() {
        List<GenderDto> gendersDto = genderService.getGenders();
        
        List<GenderRest> genderRests = new ArrayList<>();

        for (GenderDto gender: gendersDto) {
            GenderRest genderRest = mapper.map(gender, GenderRest.class);
            genderRests.add(genderRest);
        }

        return genderRests;
    }

}
