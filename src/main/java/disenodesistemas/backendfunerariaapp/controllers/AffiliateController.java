package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.AffiliateResponseDto;
import disenodesistemas.backendfunerariaapp.service.AffiliateService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/affiliates")
public class AffiliateController {

    private final AffiliateService affiliateService;

    public AffiliateController(final AffiliateService affiliateService) {
        this.affiliateService = affiliateService;
    }

    @PostMapping
    public AffiliateResponseDto createAffiliate(@RequestBody @Valid final AffiliateRequestDto affiliateRequestDto) {
        //con SecurityContextHolder accedemos al contexto de la parte de la seguridad de la app y obtenemos la autenticacion del user
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //Del metodo obtenemos el subject name que seria nuestro email
        //final String email = authentication.getName();
        //affiliateCreationDto.setUserEmail(email);
        return affiliateService.createAffiliate(affiliateRequestDto);
    }


}
