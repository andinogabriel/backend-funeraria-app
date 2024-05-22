package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.utils.SupplierTestDataFactory.getSupplierEntity;
import static disenodesistemas.backendfunerariaapp.utils.SupplierTestDataFactory.getSupplierRequestDto;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.request.SupplierRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.SupplierResponseDto;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import disenodesistemas.backendfunerariaapp.service.SupplierService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class SupplierControllerTest
    extends AbstractControllerTest<
        SupplierRequestDto, SupplierResponseDto, SupplierEntity, String> {

  @Mock private SupplierService supplierService;
  @InjectMocks private SupplierController sut;
  private final String EXISTING_SUPPLIER_IDENTIFIER = "B83762910";

  @Test
  void findAll() {
    testFindAll(
        () -> List.of(responseDto),
        sut::findAll,
        () -> List.of(responseDto),
        supplierService::findAll);
    then(supplierService).should(times(1)).findAll();
  }

  @Test
  void create() {
    testCreate(supplierService::create, sut::create, requestDto, responseDto);
    then(supplierService).should(times(1)).create(requestDto);
  }

  @Test
  void findById() {
    testFindByID(
        supplierService::findById, sut::findById, EXISTING_SUPPLIER_IDENTIFIER, responseDto);
    then(supplierService).should(times(1)).findById(EXISTING_SUPPLIER_IDENTIFIER);
  }

  @Test
  void delete() {
    testDelete(sut::delete, EXISTING_SUPPLIER_IDENTIFIER, "DELETE SUPPLIER");
    then(supplierService).should(times(1)).delete(EXISTING_SUPPLIER_IDENTIFIER);
  }

  @Test
  void update() {
    testUpdate(
        supplierService::update,
        sut::update,
        EXISTING_SUPPLIER_IDENTIFIER,
        requestDto,
        responseDto);
    then(supplierService).should(times(1)).update(EXISTING_SUPPLIER_IDENTIFIER, requestDto);
  }

  @Override
  protected SupplierRequestDto getRequestDto() {
    return getSupplierRequestDto();
  }

  @Override
  protected Class<SupplierResponseDto> getResponseDtoClass() {
    return SupplierResponseDto.class;
  }

  @Override
  protected SupplierEntity getEntity() {
    return getSupplierEntity();
  }
}
