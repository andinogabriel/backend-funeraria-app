package disenodesistemas.backendfunerariaapp.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
abstract class AbstractControllerTest<RequestDto, ResponseDto, Entity, ID> {
  protected RequestDto requestDto;
  protected ResponseDto responseDto;
  protected Entity entity;
  private ProjectionFactory projectionFactory;

  @BeforeEach
  void setUp() {
    requestDto = getRequestDto();
    entity = getEntity();
    projectionFactory = new SpelAwareProxyProjectionFactory();
    responseDto = projectionFactory.createProjection(getResponseDtoClass(), getEntity());
  }

  protected void testCreate(
      final Function<RequestDto, ResponseDto> serviceMethod,
      final Function<RequestDto, ResponseEntity<ResponseDto>> controllerMethod,
      final RequestDto requestDto,
      final ResponseDto expectedResponse) {
    given(serviceMethod.apply(requestDto)).willReturn(responseDto);
    final ResponseEntity<ResponseDto> responseEntity = controllerMethod.apply(requestDto);

    assertAll(
        () -> assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode()),
        () -> assertEquals(expectedResponse, responseEntity.getBody()));
  }

  protected void testFindAll(
      final Supplier<List<ResponseDto>> responseDtoListSupplier,
      final Supplier<ResponseEntity<List<ResponseDto>>> controllerMethod,
      final Supplier<List<ResponseDto>> serviceResultProvider,
      final Supplier<List<ResponseDto>> findAllServiceMethod) {

    final List<ResponseDto> responseDtoList = responseDtoListSupplier.get();
    given(findAllServiceMethod.get()).willReturn(serviceResultProvider.get());

    final ResponseEntity<List<ResponseDto>> responseEntity = controllerMethod.get();

    assertAll(
        () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
        () -> assertEquals(responseDtoList, responseEntity.getBody()));
  }

  protected void testUpdate(
      final BiFunction<ID, RequestDto, ResponseDto> serviceUpdateMethod,
      final BiFunction<ID, RequestDto, ResponseEntity<ResponseDto>> controllerUpdateMethod,
      final ID id,
      final RequestDto requestDto,
      final ResponseDto responseDto) {
    given(serviceUpdateMethod.apply(id, requestDto)).willReturn(responseDto);

    final ResponseEntity<ResponseDto> responseEntity = controllerUpdateMethod.apply(id, requestDto);

    assertAll(
        () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
        () -> assertEquals(responseDto, responseEntity.getBody()));
  }

  protected void testFindByID(
      final Function<ID, ResponseDto> serviceFindByIdMethod,
      final Function<ID, ResponseEntity<ResponseDto>> controllerFindByIdMethod,
      final ID id,
      final ResponseDto responseDto) {
    given(serviceFindByIdMethod.apply(id)).willReturn(responseDto);
    final ResponseEntity<ResponseDto> responseEntity = controllerFindByIdMethod.apply(id);

    assertAll(
        () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
        () -> assertEquals(responseDto, responseEntity.getBody()));
  }

  protected void testDelete(
      final Function<ID, ResponseEntity<OperationStatusModel>> controllerDeleteMethod,
      final ID id,
      final String deletedMessage) {
    final OperationStatusModel expectedOperationStatusModel =
        OperationStatusModel.builder().name(deletedMessage).result("SUCCESSFUL").build();

    final ResponseEntity<OperationStatusModel> responseEntity = controllerDeleteMethod.apply(id);

    assertAll(
        () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
        () -> assertEquals(expectedOperationStatusModel, responseEntity.getBody()));
  }

  protected abstract RequestDto getRequestDto();

  protected ResponseDto getResponseDto() {
    return projectionFactory.createProjection(getResponseDtoClass(), getEntity());
  }

  protected abstract Class<ResponseDto> getResponseDtoClass();

  protected abstract Entity getEntity();
}
