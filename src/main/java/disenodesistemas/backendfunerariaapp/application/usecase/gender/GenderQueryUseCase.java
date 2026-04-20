package disenodesistemas.backendfunerariaapp.application.usecase.gender;

import disenodesistemas.backendfunerariaapp.application.port.out.GenderPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.GenderEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.GenderMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.GenderResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenderQueryUseCase {

  private final GenderPersistencePort genderPersistencePort;
  private final GenderMapper genderMapper;

  @Transactional(readOnly = true)
  public List<GenderResponseDto> getGenders() {
    return genderPersistencePort.findAllByOrderByName().stream().map(genderMapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public GenderEntity getGenderById(final Long id) {
    return genderPersistencePort
        .findById(id)
        .orElseThrow(() -> new NotFoundException("gender.error.not.found"));
  }
}
