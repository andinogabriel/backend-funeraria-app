package disenodesistemas.backendfunerariaapp.application.usecase.funeral;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.FuneralPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.FuneralMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.FuneralResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FuneralQueryUseCase {

  private final FuneralPersistencePort funeralPersistencePort;
  private final FuneralMapper funeralMapper;
  private final AuthenticatedUserPort authenticatedUserPort;

  @Transactional(readOnly = true)
  public List<FuneralResponseDto> findAll() {
    return funeralPersistencePort.findAllByOrderByRegisterDateDesc().stream()
        .map(funeralMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public FuneralResponseDto findById(final Long id) {
    return funeralMapper.toDto(findEntityById(id));
  }

  @Transactional(readOnly = true)
  public List<FuneralResponseDto> findFuneralsByUser() {
    return funeralPersistencePort
        .findFuneralsByUserEmail(authenticatedUserPort.getAuthenticatedEmail())
        .stream()
        .map(funeralMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public Funeral findEntityById(final Long id) {
    return funeralPersistencePort
        .findById(id)
        .orElseThrow(() -> new NotFoundException("funeral.error.not.found"));
  }
}
