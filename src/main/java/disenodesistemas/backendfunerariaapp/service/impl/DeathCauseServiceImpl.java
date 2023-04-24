package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.DeathCauseDto;
import disenodesistemas.backendfunerariaapp.dto.response.DeathCauseResponseDto;
import disenodesistemas.backendfunerariaapp.entities.DeathCauseEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.DeathCauseRepository;
import disenodesistemas.backendfunerariaapp.service.DeathCauseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeathCauseServiceImpl implements DeathCauseService {

    private final DeathCauseRepository deathCauseRepository;
    private final ProjectionFactory projectionFactory;

    @Override
    @Transactional
    public DeathCauseResponseDto create(final DeathCauseDto deathCauseDto) {
        final DeathCauseEntity deathCauseEntity = new DeathCauseEntity(deathCauseDto.getName());
        return projectionFactory.createProjection(DeathCauseResponseDto.class,
                deathCauseRepository.save(deathCauseEntity));
    }

    @Override
    @Transactional
    public DeathCauseResponseDto update(final Long id, final DeathCauseDto deathCauseDto) {
        final DeathCauseEntity deathCauseToUpdate = findEntityById(id);
        deathCauseToUpdate.setName(deathCauseDto.getName());
        return projectionFactory.createProjection(DeathCauseResponseDto.class, deathCauseRepository.save(deathCauseToUpdate));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeathCauseResponseDto> findAll() {
        return deathCauseRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional
    public void delete(final Long id) {
        deathCauseRepository.delete(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public DeathCauseResponseDto findById(final Long id) {
        return projectionFactory.createProjection(DeathCauseResponseDto.class, findEntityById(id));
    }

    private DeathCauseEntity findEntityById(final Long id) {
        return deathCauseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("death.cause.not.found"));
    }
}
