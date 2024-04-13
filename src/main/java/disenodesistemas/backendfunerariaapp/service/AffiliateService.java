package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.AffiliateResponseDto;
import java.util.List;

public interface AffiliateService
    extends CommonService<AffiliateResponseDto, AffiliateRequestDto, Integer> {

  List<AffiliateResponseDto> findAllByDeceasedFalse();

  List<AffiliateResponseDto> findAffiliatesByUser();

  List<AffiliateResponseDto> findAffiliatesByFirstNameOrLastNameOrDniContaining(
      String valueToSearch);
}
