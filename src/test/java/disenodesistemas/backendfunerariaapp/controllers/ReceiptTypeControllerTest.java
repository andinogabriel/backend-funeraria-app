package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.utils.ReceiptTypeTestDataFactory.getCashReceipt;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.response.ReceiptTypeResponseDto;
import disenodesistemas.backendfunerariaapp.service.ReceiptTypeService;
import java.util.List;
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

@ExtendWith(MockitoExtension.class)
class ReceiptTypeControllerTest {

  @Mock private ReceiptTypeService receiptTypeService;
  @InjectMocks private ReceiptTypeController sut;
  private ReceiptTypeResponseDto receiptTypeResponseDto;

  @BeforeEach
  void setUp() {
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    receiptTypeResponseDto =
        projectionFactory.createProjection(ReceiptTypeResponseDto.class, getCashReceipt());
  }

  @Test
  void findAll() {
    final List<ReceiptTypeResponseDto> expecetdList = List.of(receiptTypeResponseDto);
    given(receiptTypeService.getAllReceiptTypes()).willReturn(expecetdList);
    final ResponseEntity<List<ReceiptTypeResponseDto>> actualResult = sut.findAll();

    assertAll(
        () -> assertEquals(HttpStatus.OK, actualResult.getStatusCode()),
        () -> assertEquals(expecetdList, actualResult.getBody()));
    then(receiptTypeService).should(times(1)).getAllReceiptTypes();
  }
}
