package disenodesistemas.backendfunerariaapp.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.request.DeathCauseDto;
import disenodesistemas.backendfunerariaapp.dto.response.DeathCauseResponseDto;
import disenodesistemas.backendfunerariaapp.entities.DeathCauseEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.DeathCauseRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class DeathCauseServiceImplTest {

  @Mock private DeathCauseRepository deathCauseRepository;
  @Mock private ProjectionFactory projectionFactory;
  @InjectMocks private DeathCauseServiceImpl sut;

  private DeathCauseDto deathCauseDto;
  private DeathCauseEntity deathCauseEntity;
  private DeathCauseResponseDto deathCauseResponseDto;

  @BeforeEach
  void setUp() {
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    deathCauseDto = DeathCauseDto.builder().name("Clinical death").build();
    deathCauseEntity = new DeathCauseEntity("Clinical death");
    deathCauseResponseDto =
        projectionFactory.createProjection(DeathCauseResponseDto.class, deathCauseEntity);
  }

  @Test
  void create() {
    given(deathCauseRepository.save(any(DeathCauseEntity.class))).willReturn(deathCauseEntity);
    given(projectionFactory.createProjection(DeathCauseResponseDto.class, deathCauseEntity))
        .willReturn(deathCauseResponseDto);

    final DeathCauseResponseDto actualResult = sut.create(deathCauseDto);

    assertEquals(deathCauseDto.getName(), actualResult.getName());
    then(deathCauseRepository).should(times(1)).save(any(DeathCauseEntity.class));
    then(projectionFactory)
        .should(times(1))
        .createProjection(DeathCauseResponseDto.class, deathCauseEntity);
  }

  @Test
  void update() {
    final Long id = deathCauseEntity.getId();
    given(deathCauseRepository.findById(id)).willReturn(Optional.ofNullable(deathCauseEntity));
    given(deathCauseRepository.save(deathCauseEntity)).willReturn(deathCauseEntity);
    given(projectionFactory.createProjection(DeathCauseResponseDto.class, deathCauseEntity))
        .willReturn(deathCauseResponseDto);

    final DeathCauseResponseDto actualResult = sut.update(id, deathCauseDto);

    assertAll(
        () -> assertEquals(deathCauseResponseDto.getId(), actualResult.getId()),
        () -> assertEquals(deathCauseResponseDto.getName(), actualResult.getName()));
    final InOrder inOrder = inOrder(deathCauseRepository, projectionFactory);
    then(deathCauseRepository).should(inOrder, times(1)).findById(id);
    then(deathCauseRepository).should(inOrder, times(1)).save(deathCauseEntity);
    then(projectionFactory)
        .should(inOrder, times(1))
        .createProjection(DeathCauseResponseDto.class, deathCauseEntity);
  }

  @Test
  void findAll() {
    final List<DeathCauseResponseDto> expectedResult = List.of(deathCauseResponseDto);
    given(deathCauseRepository.findAllByOrderByNameAsc()).willReturn(expectedResult);

    final List<DeathCauseResponseDto> actualResult = sut.findAll();

    assertAll(
        () -> assertEquals(expectedResult.get(0).getId(), actualResult.get(0).getId()),
        () -> assertEquals(expectedResult.get(0).getName(), actualResult.get(0).getName()));
    then(deathCauseRepository).should(times(1)).findAllByOrderByNameAsc();
  }

  @Test
  void delete() {
    final Long id = deathCauseEntity.getId();
    given(deathCauseRepository.findById(id)).willReturn(Optional.ofNullable(deathCauseEntity));
    willDoNothing().given(deathCauseRepository).delete(deathCauseEntity);

    sut.delete(id);
    final InOrder inOrder = inOrder(deathCauseRepository);
    then(deathCauseRepository).should(inOrder).findById(id);
    then(deathCauseRepository).should(inOrder).delete(deathCauseEntity);
  }

  @Test
  void findById() {
    final Long id = deathCauseDto.getId();
    given(deathCauseRepository.findById(id)).willReturn(Optional.ofNullable(deathCauseEntity));
    given(projectionFactory.createProjection(DeathCauseResponseDto.class, deathCauseEntity))
        .willReturn(deathCauseResponseDto);

    final DeathCauseResponseDto actualResult = sut.findById(id);

    assertAll(
        () -> assertEquals(id, actualResult.getId()),
        () -> assertEquals(deathCauseEntity.getName(), actualResult.getName()));
    then(deathCauseRepository).should(times(1)).findById(id);
    then(projectionFactory)
        .should(times(1))
        .createProjection(DeathCauseResponseDto.class, deathCauseEntity);
  }

  @Test
  void findByIdThrowsNotFoundError() {
    final Long NON_EXISTING_ID = 123L;
    given(deathCauseRepository.findById(NON_EXISTING_ID))
        .willThrow(new NotFoundException("death.cause.not.found"));

    final NotFoundException actualResult =
        assertThrows(NotFoundException.class, () -> sut.findById(NON_EXISTING_ID));

    assertAll(
        () -> assertEquals(HttpStatus.NOT_FOUND, actualResult.getStatus()),
        () -> assertEquals("death.cause.not.found", actualResult.getMessage()));
    then(deathCauseRepository).should(times(1)).findById(NON_EXISTING_ID);
    then(projectionFactory)
        .should(times(0))
        .createProjection(DeathCauseResponseDto.class, deathCauseEntity);
  }
}
