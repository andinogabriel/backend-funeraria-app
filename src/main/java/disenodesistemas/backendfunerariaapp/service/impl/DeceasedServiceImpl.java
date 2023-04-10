package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.DeceasedResponseDto;
import disenodesistemas.backendfunerariaapp.entities.AddressEntity;
import disenodesistemas.backendfunerariaapp.entities.DeathCauseEntity;
import disenodesistemas.backendfunerariaapp.entities.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.entities.GenderEntity;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.exceptions.ConflictException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.DeceasedRepository;
import disenodesistemas.backendfunerariaapp.service.DeceasedService;
import disenodesistemas.backendfunerariaapp.service.converters.AbstractConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeceasedServiceImpl implements DeceasedService {

    private final DeceasedRepository deceasedRepository;
    private final ProjectionFactory projectionFactory;
    private final ModelMapper mapper;
    private final AbstractConverter<DeceasedEntity, DeceasedRequestDto> converter;

    @Override
    @Transactional(readOnly = true)
    public List<DeceasedResponseDto> findAll() {
        return deceasedRepository.findAllByOrderByRegisterDateDesc();
    }

    @Override
    @Transactional
    public DeceasedResponseDto create(final DeceasedRequestDto deceasedRequest) {
        return projectionFactory.createProjection
                (DeceasedResponseDto.class,
                deceasedRepository.save(converter.fromDto(deceasedRequest))
        );
    }

    @Override
    @Transactional
    public DeceasedResponseDto update(final Integer dni, final DeceasedRequestDto deceasedRequest) {
        final DeceasedEntity entityToUpdate = getDeceasedByDni(dni);

        if (!Objects.equals(entityToUpdate.getDni(), deceasedRequest.getDni()) &&
                deceasedRepository.existsByDni(deceasedRequest.getDni()))
            throw new ConflictException("deceased.dni.already.registered");

        entityToUpdate.setDeceasedRelationship(mapper.map(deceasedRequest.getUserRelationship(), RelationshipEntity.class));
        entityToUpdate.setBirthDate(deceasedRequest.getBirthDate());
        entityToUpdate.setDni(deceasedRequest.getDni());
        entityToUpdate.setDeathCause(mapper.map(deceasedRequest.getDeathCause(), DeathCauseEntity.class));
        entityToUpdate.setDeathDate(deceasedRequest.getDeathDate());
        entityToUpdate.setFirstName(deceasedRequest.getFirstName());
        entityToUpdate.setLastName(deceasedRequest.getLastName());
        entityToUpdate.setGender(mapper.map(deceasedRequest.getGender(), GenderEntity.class));
        entityToUpdate.setPlaceOfDeath(mapper.map(deceasedRequest.getPlaceOfDeath(), AddressEntity.class));
        return projectionFactory.createProjection(DeceasedResponseDto.class, deceasedRepository.save(entityToUpdate));
    }

    @Override
    @Transactional
    public void delete(final Integer dni) {
        deceasedRepository.delete(getDeceasedByDni(dni));
    }

    @Override
    @Transactional(readOnly = true)
    public DeceasedResponseDto findByDni(final Integer dni) {
        final DeceasedEntity entity = getDeceasedByDni(dni);
        return projectionFactory.createProjection(DeceasedResponseDto.class, entity);
    }

    private DeceasedEntity getDeceasedByDni(final Integer dni) {
        return deceasedRepository.findByDni(dni)
                .orElseThrow(() -> new NotFoundException("deceased.not.found"));
    }
}
