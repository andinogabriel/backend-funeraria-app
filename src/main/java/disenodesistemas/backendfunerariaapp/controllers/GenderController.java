package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.response.GenderResponseDto;
import disenodesistemas.backendfunerariaapp.service.impl.GenderServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/genders")
public class GenderController {

    private final GenderServiceImpl genderService;

    @Autowired
    public GenderController(GenderServiceImpl genderService, ModelMapper mapper) {
        this.genderService = genderService;
    }

    @GetMapping
    public List<GenderResponseDto> getGenders() {
        return genderService.getGenders();
    }

}
