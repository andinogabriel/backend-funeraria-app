package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.DeceasedTestDataFactory.getDeceasedEntity;
import static disenodesistemas.backendfunerariaapp.utils.DeceasedTestDataFactory.getDeceasedRequestDto;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.DeceasedResponseDto;
import disenodesistemas.backendfunerariaapp.entities.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.exceptions.ConflictException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.DeceasedRepository;
import disenodesistemas.backendfunerariaapp.service.converters.AbstractConverter;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class DeceasedServiceImplTest {

  @Mock private DeceasedRepository deceasedRepository;
  @Mock private ProjectionFactory projectionFactory;
  @Mock private ModelMapper mapper;
  @Mock private AbstractConverter<DeceasedEntity, DeceasedRequestDto> converter;
  @InjectMocks private DeceasedServiceImpl sut;

  private DeceasedResponseDto deceasedResponseDto;
  private static DeceasedRequestDto deceasedRequestDto;
  private static final Integer EXISTING_DNI = 18632946;

  @BeforeEach
  void setUp() {
    deceasedRequestDto = getDeceasedRequestDto();
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    deceasedResponseDto =
        projectionFactory.createProjection(DeceasedResponseDto.class, getDeceasedRequestDto());
  }

  @Test
  void findAll() {
    final List<DeceasedResponseDto> expectedResult = List.of(deceasedResponseDto);
    given(deceasedRepository.findAllByOrderByRegisterDateDesc()).willReturn(expectedResult);

    final List<DeceasedResponseDto> actualResponse = sut.findAll();

    assertAll(
        () -> assertEquals(expectedResult.size(), actualResponse.size()),
        () -> assertEquals(expectedResult.get(0).getDni(), actualResponse.get(0).getDni()));
    then(deceasedRepository).should(only()).findAllByOrderByRegisterDateDesc();
  }

  @Test
  void create() {
    final DeceasedEntity deceasedEntity = getDeceasedEntity();
    given(converter.fromDto(deceasedRequestDto)).willReturn(deceasedEntity);
    given(deceasedRepository.save(deceasedEntity)).willReturn(deceasedEntity);
    given(projectionFactory.createProjection(DeceasedResponseDto.class, deceasedEntity))
        .willReturn(deceasedResponseDto);

    final DeceasedResponseDto actualResponse = sut.create(deceasedRequestDto);

    deceasedAsserts(deceasedRequestDto, actualResponse);

    final InOrder inOrder = inOrder(converter, deceasedRepository, projectionFactory);
    then(converter).should(inOrder, times(1)).fromDto(deceasedRequestDto);
    then(deceasedRepository).should(inOrder, times(1)).save(deceasedEntity);
    then(projectionFactory)
        .should(inOrder, times(1))
        .createProjection(DeceasedResponseDto.class, deceasedEntity);
  }

  @Test
  void update() {
    final DeceasedEntity deceasedEntity = getDeceasedEntity();
    given(deceasedRepository.findByDni(EXISTING_DNI)).willReturn(Optional.of(deceasedEntity));
    given(projectionFactory.createProjection(DeceasedResponseDto.class, deceasedEntity))
        .willReturn(deceasedResponseDto);
    given(deceasedRepository.save(deceasedEntity)).willReturn(deceasedEntity);
    given(projectionFactory.createProjection(DeceasedResponseDto.class, deceasedEntity))
        .willReturn(deceasedResponseDto);

    final DeceasedResponseDto actualResponse = sut.update(EXISTING_DNI, deceasedRequestDto);
    deceasedAsserts(deceasedRequestDto, actualResponse);
    then(deceasedRepository).should(times(1)).save(deceasedEntity);
    then(deceasedRepository).should(times(1)).findByDni(EXISTING_DNI);
    then(projectionFactory)
        .should(times(1))
        .createProjection(DeceasedResponseDto.class, deceasedEntity);
  }

  @Test
  void updateThrowsConflictException() {
    final DeceasedEntity deceasedEntity = getDeceasedEntity();
    final Integer anotherDni = 12836941;
    deceasedEntity.setDni(anotherDni);

    given(deceasedRepository.findByDni(EXISTING_DNI)).willReturn(Optional.of(deceasedEntity));
    given(deceasedRepository.existsByDni(EXISTING_DNI)).willReturn(Boolean.TRUE);

    final ConflictException actualResponse =
        assertThrows(ConflictException.class, () -> sut.update(EXISTING_DNI, deceasedRequestDto));

    assertAll(
        () -> assertEquals(HttpStatus.CONFLICT, actualResponse.getStatus()),
        () -> assertEquals("deceased.dni.already.registered", actualResponse.getMessage()));

    then(deceasedRepository).should(times(1)).findByDni(EXISTING_DNI);
    then(deceasedRepository).should(times(1)).existsByDni(EXISTING_DNI);
    then(deceasedRepository).should(never()).save(deceasedEntity);
    then(projectionFactory)
        .should(never())
        .createProjection(DeceasedResponseDto.class, deceasedEntity);
  }

  @Test
  void delete() {
    final DeceasedEntity deceasedEntity = getDeceasedEntity();
    given(deceasedRepository.findByDni(EXISTING_DNI)).willReturn(Optional.of(deceasedEntity));

    sut.delete(EXISTING_DNI);

    then(deceasedRepository).should(times(1)).findByDni(EXISTING_DNI);
    then(deceasedRepository).should(times(1)).delete(deceasedEntity);
  }

  @Test
  void findByDni() {
    final DeceasedEntity deceasedEntity = getDeceasedEntity();
    given(deceasedRepository.findByDni(EXISTING_DNI)).willReturn(Optional.of(deceasedEntity));
    given(projectionFactory.createProjection(DeceasedResponseDto.class, deceasedEntity))
        .willReturn(deceasedResponseDto);

    final DeceasedResponseDto actualResult = sut.findByDni(EXISTING_DNI);

    assertAll(
        () -> assertEquals(EXISTING_DNI, actualResult.getDni()),
        () -> assertEquals(deceasedEntity.getLastName(), actualResult.getLastName()),
        () -> assertEquals(deceasedEntity.getFirstName(), actualResult.getFirstName()));
    then(deceasedRepository).should(times(1)).findByDni(EXISTING_DNI);
    then(projectionFactory)
        .should(times(1))
        .createProjection(DeceasedResponseDto.class, deceasedEntity);
  }

  @Test
  void findByDniThrowsException() {
    final Integer NON_EXISTING_DNI = 621564;
    willThrow(new NotFoundException("deceased.not.found"))
        .given(deceasedRepository)
        .findByDni(NON_EXISTING_DNI);

    final NotFoundException exception =
        assertThrows(NotFoundException.class, () -> sut.findByDni(NON_EXISTING_DNI));

    assertAll(
        () -> assertEquals(HttpStatus.NOT_FOUND, exception.getStatus()),
        () -> assertEquals("deceased.not.found", exception.getMessage()));

    then(deceasedRepository).should(times(1)).findByDni(NON_EXISTING_DNI);
    then(projectionFactory)
        .should(never())
        .createProjection(eq(DeceasedResponseDto.class), isA(DeceasedEntity.class));
  }

  private static void deceasedAsserts(
      final DeceasedRequestDto deceasedRequestDto, final DeceasedResponseDto actualResponse) {
    assertAll(
        () -> assertEquals(deceasedRequestDto.getLastName(), actualResponse.getLastName()),
        () -> assertEquals(deceasedRequestDto.getFirstName(), actualResponse.getFirstName()),
        () -> assertEquals(deceasedRequestDto.getDni(), actualResponse.getDni()),
        () -> assertEquals(deceasedRequestDto.getBirthDate(), actualResponse.getBirthDate()),
        () -> assertEquals(deceasedRequestDto.getDeathDate(), actualResponse.getDeathDate()),
        () -> assertNotNull(actualResponse.getPlaceOfDeath()),
        () ->
            assertEquals(
                deceasedRequestDto.getDeceasedRelationship().getName(),
                actualResponse.getDeceasedRelationship().getName()),
        () ->
            assertEquals(
                deceasedRequestDto.getDeathCause().getName(),
                actualResponse.getDeathCause().getName()),
        () ->
            assertEquals(
                deceasedRequestDto.getGender().getName(), actualResponse.getGender().getName()),
        () ->
            assertEquals(
                deceasedRequestDto.getDeceasedUser().getEmail(),
                actualResponse.getDeceasedUser().getEmail()));
  }
}
