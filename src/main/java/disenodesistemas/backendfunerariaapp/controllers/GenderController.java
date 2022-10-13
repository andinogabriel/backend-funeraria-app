package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.response.GenderResponseDto;
import disenodesistemas.backendfunerariaapp.service.GenderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/genders")
public class GenderController {

    private final GenderService genderService;

    public GenderController(final GenderService genderService) {
        this.genderService = genderService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping
    public List<GenderResponseDto> getGenders() {
        return genderService.getGenders();
    }

}
