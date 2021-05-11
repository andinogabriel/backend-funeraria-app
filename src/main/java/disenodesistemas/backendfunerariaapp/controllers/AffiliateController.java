package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.AffiliateCreationDto;
import disenodesistemas.backendfunerariaapp.dto.AffiliateDto;
import disenodesistemas.backendfunerariaapp.models.requests.AffiliateDetailsRequestModel;
import disenodesistemas.backendfunerariaapp.models.responses.AffiliateRest;
import disenodesistemas.backendfunerariaapp.service.AffiliateService;
import org.modelmapper.ModelMapper;
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

    @Autowired
    AffiliateService affiliateService;

    @Autowired
    ModelMapper mapper;


    @PostMapping
    public AffiliateRest createAffiliate(@RequestBody @Valid AffiliateDetailsRequestModel createAffiliate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getPrincipal().toString();

        AffiliateCreationDto affiliateCreationDto = mapper.map(createAffiliate, AffiliateCreationDto.class);
        affiliateCreationDto.setUserEmail(email);
        AffiliateDto affiliateDto = affiliateService.createAffiliate(affiliateCreationDto);

        return mapper.map(affiliateDto, AffiliateRest.class);
    }


}
