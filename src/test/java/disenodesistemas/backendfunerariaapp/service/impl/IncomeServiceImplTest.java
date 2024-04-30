package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.IncomeDetailTestDataFactory.getIncomeDetailDto;
import static disenodesistemas.backendfunerariaapp.utils.IncomeDetailTestDataFactory.getIncomeDetails;
import static disenodesistemas.backendfunerariaapp.utils.ReceiptTypeTestDataFactory.getIncomeCashReceipt;
import static disenodesistemas.backendfunerariaapp.utils.SupplierTestDataFactory.getSupplierRequestDto;
import static disenodesistemas.backendfunerariaapp.utils.UserTestDataFactory.getUserDto;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import disenodesistemas.backendfunerariaapp.dto.request.IncomeDetailRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.IncomeResponseDto;
import disenodesistemas.backendfunerariaapp.entities.*;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.IncomeRepository;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import disenodesistemas.backendfunerariaapp.repository.PlanRepository;
import disenodesistemas.backendfunerariaapp.service.InvoiceService;
import disenodesistemas.backendfunerariaapp.service.PlanService;
import disenodesistemas.backendfunerariaapp.service.SupplierService;
import disenodesistemas.backendfunerariaapp.service.UserService;
import disenodesistemas.backendfunerariaapp.service.converters.AbstractConverter;
import disenodesistemas.backendfunerariaapp.utils.IncomeDetailTestDataFactory;
import disenodesistemas.backendfunerariaapp.utils.IncomeTestDataFactory;
import disenodesistemas.backendfunerariaapp.utils.ItemTestDataFactory;
import disenodesistemas.backendfunerariaapp.utils.PlanTestDataFactory;
import disenodesistemas.backendfunerariaapp.utils.ReceiptTypeTestDataFactory;
import disenodesistemas.backendfunerariaapp.utils.SupplierTestDataFactory;
import disenodesistemas.backendfunerariaapp.utils.UserTestDataFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IncomeServiceImplTest {

  @Mock private IncomeRepository incomeRepository;
  @Mock private ItemRepository itemRepository;
  @Mock private InvoiceService invoiceService;
  @Mock private PlanService planService;
  @Mock private PlanRepository planRepository;
  @Mock private UserService userService;
  @Mock private SupplierService supplierService;
  @Mock private ProjectionFactory projectionFactory;
  @Mock private ModelMapper modelMapper;
  @Mock private AbstractConverter<IncomeDetailEntity, IncomeDetailRequestDto> incomeDetailConverter;

  @InjectMocks private IncomeServiceImpl sut;

  private IncomeRequestDto incomeRequestDto;

  private IncomeResponseDto incomeResponseDto;

  @BeforeEach
  void setUp() {
    incomeRequestDto =
        IncomeRequestDto.builder()
            // .receiptNumber(123456789L)
            .tax(BigDecimal.TEN)
            // .receiptSeries(321L)
            .receiptType(getIncomeCashReceipt())
            .incomeUser(getUserDto())
            .supplier(getSupplierRequestDto())
            .incomeDetails(getIncomeDetails())
            .build();

    given(userService.getUserByEmail(getUserDto().getEmail()))
        .willReturn(UserTestDataFactory.getUserEntity());
    given(modelMapper.map(getIncomeCashReceipt(), ReceiptTypeEntity.class))
        .willReturn(ReceiptTypeTestDataFactory.getCashReceipt());
    given(supplierService.findSupplierEntityByNif(getSupplierRequestDto().getNif()))
        .willReturn(SupplierTestDataFactory.getSupplierEntity());
    final ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
    incomeResponseDto =
        factory.createProjection(IncomeResponseDto.class, IncomeTestDataFactory.getIncome());
  }

  @Test
  void create() {
    final List<ItemEntity> itemsEntity = List.of(ItemTestDataFactory.getItem());
    given(invoiceService.createSerialNumber()).willReturn(321L);
    given(invoiceService.createReceiptNumber()).willReturn(123456789L);
    given(itemRepository.findAll()).willReturn(List.of(ItemTestDataFactory.getItem()));
    given(modelMapper.map(getIncomeDetailDto(), IncomeDetailEntity.class))
        .willReturn(IncomeDetailTestDataFactory.getIncomeDetail());
    given(incomeRepository.save(IncomeTestDataFactory.getIncome()))
        .willReturn(IncomeTestDataFactory.getIncome());
    given(incomeDetailConverter.fromDTOs(incomeRequestDto.getIncomeDetails()))
        .willReturn(List.of(IncomeDetailTestDataFactory.getIncomeDetail()));
    given(itemRepository.saveAll(itemsEntity)).willReturn(itemsEntity);
    doNothing().when(planService).updatePlansPrice(itemsEntity);
    given(planRepository.findPlansContainingAnyOfThisItems(itemsEntity))
        .willReturn(List.of(PlanTestDataFactory.getPlan()));
    given(planRepository.saveAll(List.of(PlanTestDataFactory.getPlan())))
        .willReturn(List.of(PlanTestDataFactory.getPlan()));
    given(incomeRepository.save(any(IncomeEntity.class)))
        .willReturn(IncomeTestDataFactory.getIncome());
    given(
            projectionFactory.createProjection(
                IncomeResponseDto.class, IncomeTestDataFactory.getIncome()))
        .willReturn(incomeResponseDto);

    final IncomeResponseDto response = sut.create(incomeRequestDto);

    assertAll(
        () ->
            assertEquals(
                incomeRequestDto.getIncomeDetails().size(), response.getIncomeDetails().size()),
        () -> assertEquals(incomeRequestDto.getTax(), response.getTax()),
        () ->
            assertEquals(
                incomeRequestDto.getIncomeUser().getEmail(), response.getIncomeUser().getEmail()),
        () ->
            assertEquals(incomeRequestDto.getSupplier().getNif(), response.getSupplier().getNif()),
        () -> assertNotNull(response.getTotalAmount()));
    verify(incomeRepository, times(2)).save(any(IncomeEntity.class));
    verify(itemRepository, only()).saveAll(itemsEntity);
  }

  @Test
  void delete() {
    Optional<IncomeEntity> incomeEntityOptional = Optional.of(new IncomeEntity());
    given(incomeRepository.findByReceiptNumber(anyLong())).willReturn(incomeEntityOptional);
    sut.delete(anyLong());
    verify(incomeRepository).findByReceiptNumber(anyLong());
    verify(incomeRepository).delete(any(IncomeEntity.class));
  }

  @Test
  void deleteThrowsError() {
    given(incomeRepository.findByReceiptNumber(anyLong())).willThrow(AppException.class);
    verify(incomeRepository, never()).delete(any(IncomeEntity.class));
  }

  @Test
  void getAllIncomes() {
    given(incomeRepository.findAllByOrderByIdDesc()).willReturn(List.of(incomeResponseDto));
    given(sut.findAll()).willReturn(List.of(incomeResponseDto));

    final List<IncomeResponseDto> incomes = sut.findAll();

    assertFalse(incomes.isEmpty());
    verify(incomeRepository, only()).findAllByOrderByIdDesc();
  }
}
