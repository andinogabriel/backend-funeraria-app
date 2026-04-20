package disenodesistemas.backendfunerariaapp.application.service;

import disenodesistemas.backendfunerariaapp.web.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.AffiliateResponseDto;
import java.util.List;

public interface AffiliateService {

  AffiliateResponseDto create(AffiliateRequestDto dto);

  AffiliateResponseDto update(Integer dni, AffiliateRequestDto dto);

  void delete(Integer dni);

  List<AffiliateResponseDto> findAll();

  AffiliateResponseDto findById(Integer dni);

  List<AffiliateResponseDto> findAllByDeceasedFalse();

  List<AffiliateResponseDto> findAffiliatesByUser();

  List<AffiliateResponseDto> findAffiliatesByFirstNameOrLastNameOrDniContaining(
      String valueToSearch);
}
