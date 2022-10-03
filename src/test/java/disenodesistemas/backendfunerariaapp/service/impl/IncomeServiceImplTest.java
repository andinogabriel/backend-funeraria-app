package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.ReceiptTypeDtoMother;
import disenodesistemas.backendfunerariaapp.dto.UserDtoMother;
import disenodesistemas.backendfunerariaapp.dto.request.IncomeDetailRequestDtoMother;
import disenodesistemas.backendfunerariaapp.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.SupplierRequestDtoMother;
import disenodesistemas.backendfunerariaapp.dto.response.IncomeResponseDto;
import disenodesistemas.backendfunerariaapp.entities.IncomeDetailEntity;
import disenodesistemas.backendfunerariaapp.entities.IncomeDetailEntityMother;
import disenodesistemas.backendfunerariaapp.entities.IncomeEntity;
import disenodesistemas.backendfunerariaapp.entities.IncomeEntityMother;
import disenodesistemas.backendfunerariaapp.entities.ItemEntityMother;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntityMother;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntityMother;
import disenodesistemas.backendfunerariaapp.entities.UserEntityMother;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.IncomeRepository;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.SupplierService;
import disenodesistemas.backendfunerariaapp.service.Interface.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IncomeServiceImplTest {

    @Mock
    private IncomeRepository incomeRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserService userService;
    @Mock
    private SupplierService supplierService;
    @Mock
    private ProjectionFactory projectionFactory;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private IncomeServiceImpl sut;

    private IncomeRequestDto incomeRequestDto;
    private ProjectionFactory factory;

    private IncomeResponseDto incomeResponseDto;

    @BeforeEach
    void setUp() {
        incomeRequestDto = IncomeRequestDto.builder()
                .receiptNumber(123456789L)
                .tax(BigDecimal.TEN)
                .receiptSeries(321L)
                .receiptType(ReceiptTypeDtoMother.getReciboDeCaja())
                .incomeUser(UserDtoMother.getUserDto())
                .supplier(SupplierRequestDtoMother.getSupplier())
                .incomeDetails(IncomeDetailRequestDtoMother.getIncomeDetails())
                .build();

        given(userService.getUserByEmail(UserDtoMother.getUserDto().getEmail())).willReturn(UserEntityMother.getUser());
        given(modelMapper.map(ReceiptTypeDtoMother.getReciboDeCaja(), ReceiptTypeEntity.class))
                .willReturn(ReceiptTypeEntityMother.getReceipt());
        given(supplierService.findSupplierEntityByNif(SupplierRequestDtoMother.getSupplier().getNif()))
                .willReturn(SupplierEntityMother.getSupplier());
        factory = new SpelAwareProxyProjectionFactory();
        incomeResponseDto = factory.createProjection(IncomeResponseDto.class,
                IncomeEntityMother.getIncome());
    }

    @Test
    void create() {
        given(incomeRepository.existsByReceiptNumber(incomeRequestDto.getReceiptNumber())).willReturn(Boolean.FALSE);
        given(itemRepository.findAll()).willReturn(List.of(ItemEntityMother.getItem()));
        given(modelMapper.map(IncomeDetailRequestDtoMother.getIncomeDetail(),IncomeDetailEntity.class))
                .willReturn(IncomeDetailEntityMother.getIncomeDetail());
        given(incomeRepository.save(IncomeEntityMother.getIncome())).willReturn(IncomeEntityMother.getIncome());
        given(projectionFactory.createProjection(IncomeResponseDto.class, IncomeEntityMother.getIncome()))
                .willReturn(incomeResponseDto);

        final IncomeResponseDto response = sut.createIncome(incomeRequestDto);

        assertAll(
                () -> assertEquals(incomeRequestDto.getIncomeDetails().size(), response.getIncomeDetails().size()),
                () -> assertEquals(incomeRequestDto.getTax(), response.getTax()),
                () -> assertEquals(incomeRequestDto.getIncomeUser().getEmail(), response.getIncomeUser().getEmail()),
                () -> assertEquals(incomeRequestDto.getSupplier().getNif(), response.getSupplier().getNif()),
                () -> assertNotNull(response.getTotalAmount())
        );
        verify(incomeRepository, times(2)).save(IncomeEntityMother.getIncome());
        verify(itemRepository).saveAll(anyList());
    }

    @DisplayName("Given a incomeRequestDto with an existing receipt number when call create method then return an exception")
    @Test
    void createIncomeWithAnExistentReceiptNumber() {
        given(incomeRepository.existsByReceiptNumber(anyLong())).willReturn(Boolean.TRUE);
        assertThrows(AppException.class, () ->  sut.createIncome(incomeRequestDto));
    }

    @Test
    void delete() {
        Optional<IncomeEntity> incomeEntityOptional = Optional.of(new IncomeEntity());
        given(incomeRepository.findByReceiptNumber(anyLong())).willReturn(incomeEntityOptional);
        sut.deleteIncome(anyLong());
        verify(incomeRepository).findByReceiptNumber(anyLong());
        verify(incomeRepository).delete(any(IncomeEntity.class));
    }

    @Test
    void deleteThrowsError() {
        given(incomeRepository.findByReceiptNumber(anyLong())).willThrow(AppException.class);
        assertThrows(AppException.class, () -> sut.deleteIncome(anyLong()));
        verify(incomeRepository, never()).delete(any(IncomeEntity.class));
    }

    @Test
    void getAllIncomes() {
        given(incomeRepository.findAllByOrderByIdDesc()).willReturn(List.of(incomeResponseDto));
        given(sut.getAllIncomes()).willReturn(List.of(incomeResponseDto));

        final List<IncomeResponseDto> incomes = sut.getAllIncomes();

        assertTrue(incomes.size() > 0);
        verify(incomeRepository).findAllByOrderByIdDesc();
    }
}