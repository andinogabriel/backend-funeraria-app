package disenodesistemas.backendfunerariaapp.application.usecase.deceased;

import disenodesistemas.backendfunerariaapp.application.port.out.DeceasedPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.DeceasedMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeceasedResponseDto;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeceasedCommandUseCase {

  private final DeceasedPersistencePort deceasedPersistencePort;
  private final DeceasedMapper deceasedMapper;
  private final DeceasedQueryUseCase deceasedQueryUseCase;

  @Transactional
  public DeceasedResponseDto create(final DeceasedRequestDto deceasedRequest) {
    final DeceasedEntity entity = deceasedMapper.toEntity(deceasedRequest);
    return deceasedMapper.toDto(deceasedPersistencePort.save(entity));
  }

  @Transactional
  public DeceasedResponseDto update(final Integer dni, final DeceasedRequestDto deceasedRequest) {
    final DeceasedEntity entityToUpdate = deceasedQueryUseCase.getDeceasedByDni(dni);

    if (!Objects.equals(entityToUpdate.getDni(), deceasedRequest.dni())
        && deceasedPersistencePort.existsByDni(deceasedRequest.dni())) {
      throw new ConflictException("deceased.dni.already.registered");
    }

    deceasedMapper.updateEntity(deceasedRequest, entityToUpdate);
    return deceasedMapper.toDto(deceasedPersistencePort.save(entityToUpdate));
  }

  @Transactional
  public void delete(final Integer dni) {
    deceasedPersistencePort.delete(deceasedQueryUseCase.getDeceasedByDni(dni));
  }
}
