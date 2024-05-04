package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.response.ProvinceResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.ProvinceRepository;
import disenodesistemas.backendfunerariaapp.service.ProvinceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProvinceServiceImpl implements ProvinceService {

  private final ProvinceRepository provinceRepository;

  @Override
  public List<ProvinceResponseDto> getAllProvinces() {
    return provinceRepository.findAllByOrderByName();
  }

  @Override
  public ProvinceEntity getProvinceById(final Long id) {
    return provinceRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("province.error.not.found"));
  }
}
