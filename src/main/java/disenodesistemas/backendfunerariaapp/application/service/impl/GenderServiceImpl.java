package disenodesistemas.backendfunerariaapp.application.service.impl;

import disenodesistemas.backendfunerariaapp.application.service.GenderService;
import disenodesistemas.backendfunerariaapp.application.usecase.gender.GenderQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.GenderEntity;
import disenodesistemas.backendfunerariaapp.web.dto.response.GenderResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenderServiceImpl implements GenderService {
  private final GenderQueryUseCase genderQueryUseCase;

  @Override
  public List<GenderResponseDto> getGenders() {
    return genderQueryUseCase.getGenders();
  }

  @Override
  public GenderEntity getGenderById(final Long id) {
    return genderQueryUseCase.getGenderById(id);
  }
}
