package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.AffiliateCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.AffiliateResponseDto;
import disenodesistemas.backendfunerariaapp.service.Interface.IAffiliate;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final IAffiliate affiliateService;

    @Autowired
    public AffiliateController(IAffiliate affiliateService) {
        this.affiliateService = affiliateService;
    }

    @PostMapping
    public AffiliateResponseDto createAffiliate(@RequestBody @Valid AffiliateCreationDto affiliateCreationDto) {
        //con SecurityContextHolder accedemos al contexto de la parte de la seguridad de la app y obtenemos la autenticacion del user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //Del metodo obtenemos el subject name que seria nuestro email
        String email = authentication.getName();
        affiliateCreationDto.setUserEmail(email);
        return affiliateService.createAffiliate(affiliateCreationDto);
    }


}
