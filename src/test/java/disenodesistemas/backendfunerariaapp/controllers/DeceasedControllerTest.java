package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.DeceasedResponseDto;
import disenodesistemas.backendfunerariaapp.entities.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.service.DeceasedService;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
class DeceasedControllerTest {

  @Mock private DeceasedService deceasedService;
  @InjectMocks private DeceasedController sut;

  private DeceasedRequestDto deceasedRequestDto;
  private DeceasedResponseDto deceasedResponseDto;
  private static final Integer DNI = 12345678;
  private static final Integer NOT_FOUND_DNI = 987654321;
  private static final String LAST_NAME = "John";
  private static final String FIRST_NAME = "Doe";

  @BeforeEach
  void setUp() {
    deceasedRequestDto =
        DeceasedRequestDto.builder().dni(DNI).firstName(LAST_NAME).lastName(FIRST_NAME).build();
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    final DeceasedEntity deceasedEntity = new DeceasedEntity();
    deceasedEntity.setLastName(LAST_NAME);
    deceasedEntity.setFirstName(FIRST_NAME);
    deceasedEntity.setDni(DNI);
    deceasedResponseDto =
        projectionFactory.createProjection(DeceasedResponseDto.class, deceasedEntity);
  }

  @Test
  void testFindAll() {
    when(deceasedService.findAll()).thenReturn(List.of(deceasedResponseDto));

    final ResponseEntity<List<DeceasedResponseDto>> responseEntity = sut.findAll();

    assertEquals(OK, responseEntity.getStatusCode());
    assertEquals(List.of(deceasedResponseDto), responseEntity.getBody());
  }

  @Test
  void testFindByDni() {
    when(deceasedService.findByDni(DNI)).thenReturn(deceasedResponseDto);

    final ResponseEntity<DeceasedResponseDto> responseEntity = sut.findByDni(DNI);

    assertEquals(OK, responseEntity.getStatusCode());
    assertEquals(deceasedResponseDto, responseEntity.getBody());
  }

  @Test
  void testFindByDniNotFound() {
    when(deceasedService.findByDni(NOT_FOUND_DNI)).thenThrow(NotFoundException.class);
    assertThrows(NotFoundException.class, () -> sut.findByDni(NOT_FOUND_DNI));
  }

  @Test
  void testCreate() {
    when(deceasedService.create(deceasedRequestDto)).thenReturn(deceasedResponseDto);

    final ResponseEntity<DeceasedResponseDto> responseEntity = sut.create(deceasedRequestDto);

    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    assertEquals(deceasedResponseDto, responseEntity.getBody());
  }

  @Test
  void testUpdate() {
    when(deceasedService.update(DNI, deceasedRequestDto)).thenReturn(deceasedResponseDto);

    final ResponseEntity<DeceasedResponseDto> responseEntity = sut.update(DNI, deceasedRequestDto);

    assertEquals(OK, responseEntity.getStatusCode());
    assertEquals(deceasedResponseDto, responseEntity.getBody());
  }

  @Test
  void testDelete() {
    final OperationStatusModel expectedOperationStatusModel =
        OperationStatusModel.builder().name("DELETE DECEASED").result("SUCCESSFUL").build();

    final ResponseEntity<OperationStatusModel> responseEntity = sut.delete(DNI);

    assertEquals(OK, responseEntity.getStatusCode());
    assertEquals(expectedOperationStatusModel, responseEntity.getBody());
  }
}
