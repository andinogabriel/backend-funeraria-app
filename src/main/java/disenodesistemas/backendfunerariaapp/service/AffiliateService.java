package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.AffiliateResponseDto;

import java.util.List;

public interface AffiliateService {
    AffiliateResponseDto createAffiliate(final AffiliateRequestDto affiliate);
    AffiliateResponseDto update(final Integer dni, final AffiliateRequestDto affiliate);
    void delete(final Integer dni);
    List<AffiliateResponseDto> findAllByDeceasedFalse();
    List<AffiliateResponseDto> findAll();
    List<AffiliateResponseDto> findAffiliatesByUser();
    List<AffiliateResponseDto> findAffiliatesByFirstNameOrLastNameOrDniContaining(String valueToSearch);

}
