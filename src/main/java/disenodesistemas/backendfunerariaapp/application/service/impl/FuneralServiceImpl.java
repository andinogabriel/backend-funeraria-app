package disenodesistemas.backendfunerariaapp.application.service.impl;

import disenodesistemas.backendfunerariaapp.application.service.FuneralService;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.request.FuneralRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.FuneralResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FuneralServiceImpl implements FuneralService {

  private final FuneralCommandUseCase funeralCommandUseCase;
  private final FuneralQueryUseCase funeralQueryUseCase;

  @Override
  public FuneralResponseDto create(final FuneralRequestDto funeralRequest) {
    return funeralCommandUseCase.create(funeralRequest);
  }

  @Override
  public FuneralResponseDto update(final Long id, final FuneralRequestDto funeralRequest) {
    return funeralCommandUseCase.update(id, funeralRequest);
  }

  @Override
  public void delete(final Long id) {
    funeralCommandUseCase.delete(id);
  }

  @Override
  public List<FuneralResponseDto> findAll() {
    return funeralQueryUseCase.findAll();
  }

  @Override
  public FuneralResponseDto findById(final Long id) {
    return funeralQueryUseCase.findById(id);
  }

  @Override
  public List<FuneralResponseDto> findFuneralsByUser() {
    return funeralQueryUseCase.findFuneralsByUser();
  }
}
