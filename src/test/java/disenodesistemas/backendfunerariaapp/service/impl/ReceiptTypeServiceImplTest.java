package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.ReceiptTypeTestDataFactory.getCashReceipt;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.response.ReceiptTypeResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.ReceiptTypeRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ReceiptTypeServiceImplTest {

  @Mock private ReceiptTypeRepository receiptTypeRepository;
  @InjectMocks private ReceiptTypeServiceImpl sut;
  private ReceiptTypeResponseDto receiptTypeResponseDto;
  private ReceiptTypeEntity receiptTypeEntity;

  @BeforeEach
  void setUp() {
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    receiptTypeEntity = getCashReceipt();
    receiptTypeResponseDto =
        projectionFactory.createProjection(ReceiptTypeResponseDto.class, receiptTypeEntity);
  }

  @Test
  void getAllReceiptTypes() {
    final List<ReceiptTypeResponseDto> expectedResult = List.of(receiptTypeResponseDto);
    given(receiptTypeRepository.findAllByOrderByName()).willReturn(expectedResult);

    final List<ReceiptTypeResponseDto> actualResult = sut.getAllReceiptTypes();

    assertAll(
        () -> assertFalse(actualResult.isEmpty()), () -> assertSame(expectedResult, actualResult));
    then(receiptTypeRepository).should(times(1)).findAllByOrderByName();
  }

  @Test
  void findByNameIsContainingIgnoreCase() {
    final String receiptTypeName = receiptTypeEntity.getName();
    given(receiptTypeRepository.findByNameIsContainingIgnoreCase(receiptTypeName))
        .willReturn(Optional.of(receiptTypeEntity));

    final ReceiptTypeEntity actualResult = sut.findByNameIsContainingIgnoreCase(receiptTypeName);

    assertEquals(receiptTypeName, actualResult.getName());
    then(receiptTypeRepository).should(times(1)).findByNameIsContainingIgnoreCase(receiptTypeName);
  }

  void findByNameIsContainingIgnoreCaseThrowsNotFoundException() {
    final String NON_EXISTING_RECEIPT_TYPE_NAME = "non-existent-name";
    given(receiptTypeRepository.findByNameIsContainingIgnoreCase(NON_EXISTING_RECEIPT_TYPE_NAME))
        .willThrow(new NotFoundException("receiptType.error.name.not.found"));

    final NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> sut.findByNameIsContainingIgnoreCase(NON_EXISTING_RECEIPT_TYPE_NAME));

    assertAll(
        () -> assertEquals(HttpStatus.NOT_FOUND, exception.getStatus()),
        () -> assertEquals("receiptType.error.name.not.found", exception.getMessage()));
    then(receiptTypeRepository)
        .should(times(1))
        .findByNameIsContainingIgnoreCase(NON_EXISTING_RECEIPT_TYPE_NAME);
  }
}
