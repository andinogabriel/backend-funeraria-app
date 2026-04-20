package disenodesistemas.backendfunerariaapp.application.service.impl;

import disenodesistemas.backendfunerariaapp.application.service.AffiliateService;
import disenodesistemas.backendfunerariaapp.application.usecase.affiliate.AffiliateCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.affiliate.AffiliateQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.AffiliateResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AffiliateServiceImpl implements AffiliateService {

  private final AffiliateCommandUseCase affiliateCommandUseCase;
  private final AffiliateQueryUseCase affiliateQueryUseCase;

  @Override
  public AffiliateResponseDto create(final AffiliateRequestDto affiliate) {
    return affiliateCommandUseCase.create(affiliate);
  }

  @Override
  public AffiliateResponseDto update(final Integer dni, final AffiliateRequestDto affiliate) {
    return affiliateCommandUseCase.update(dni, affiliate);
  }

  @Override
  public void delete(final Integer dni) {
    affiliateCommandUseCase.delete(dni);
  }

  @Override
  public List<AffiliateResponseDto> findAllByDeceasedFalse() {
    return affiliateQueryUseCase.findAllByDeceasedFalse();
  }

  @Override
  public List<AffiliateResponseDto> findAll() {
    return affiliateQueryUseCase.findAll();
  }

  @Override
  public AffiliateResponseDto findById(final Integer dni) {
    return affiliateQueryUseCase.findById(dni);
  }

  @Override
  public List<AffiliateResponseDto> findAffiliatesByUser() {
    return affiliateQueryUseCase.findAffiliatesByUser();
  }

  @Override
  public List<AffiliateResponseDto> findAffiliatesByFirstNameOrLastNameOrDniContaining(
      final String valueToSearch) {
    return affiliateQueryUseCase.findAffiliatesByFirstNameOrLastNameOrDniContaining(valueToSearch);
  }
}

