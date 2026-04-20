package disenodesistemas.backendfunerariaapp.application.service.impl;

import disenodesistemas.backendfunerariaapp.application.service.DeceasedService;
import disenodesistemas.backendfunerariaapp.application.usecase.deceased.DeceasedCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.deceased.DeceasedQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeceasedResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeceasedServiceImpl implements DeceasedService {

  private final DeceasedCommandUseCase deceasedCommandUseCase;
  private final DeceasedQueryUseCase deceasedQueryUseCase;

  @Override
  public List<DeceasedResponseDto> findAll() {
    return deceasedQueryUseCase.findAll();
  }

  @Override
  public DeceasedResponseDto create(final DeceasedRequestDto deceasedRequest) {
    return deceasedCommandUseCase.create(deceasedRequest);
  }

  @Override
  public DeceasedResponseDto update(final Integer dni, final DeceasedRequestDto deceasedRequest) {
    return deceasedCommandUseCase.update(dni, deceasedRequest);
  }

  @Override
  public void delete(final Integer dni) {
    deceasedCommandUseCase.delete(dni);
  }

  @Override
  public DeceasedResponseDto findById(final Integer dni) {
    return deceasedQueryUseCase.findById(dni);
  }
}
