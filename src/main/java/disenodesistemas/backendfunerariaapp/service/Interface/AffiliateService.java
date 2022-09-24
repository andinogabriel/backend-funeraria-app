package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.AffiliateResponseDto;

public interface AffiliateService {

    AffiliateResponseDto createAffiliate(AffiliateRequestDto affiliate);

}
