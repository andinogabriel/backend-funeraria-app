package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.response.GenderResponseDto;
import disenodesistemas.backendfunerariaapp.entities.GenderEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.GenderRepository;
import disenodesistemas.backendfunerariaapp.service.GenderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenderServiceImpl implements GenderService {

  private final GenderRepository genderRepository;

  @Override
  @Transactional(readOnly = true)
  public List<GenderResponseDto> getGenders() {
    return genderRepository.findAllProjectedBy();
  }

  @Override
  @Transactional(readOnly = true)
  public GenderEntity getGenderById(final Long id) {
    return genderRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("gender.error.not.found"));
  }
}
