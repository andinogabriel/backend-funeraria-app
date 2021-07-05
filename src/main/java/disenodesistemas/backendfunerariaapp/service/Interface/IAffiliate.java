package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.AffiliateCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.AffiliateResponseDto;

public interface IAffiliate {

    AffiliateResponseDto createAffiliate(AffiliateCreationDto affiliate);

}
