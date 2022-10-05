package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.controllers.converters.AbstractConverter;
import disenodesistemas.backendfunerariaapp.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.IncomeDetailRequestDto;;
import disenodesistemas.backendfunerariaapp.dto.response.IncomeResponseDto;
import disenodesistemas.backendfunerariaapp.entities.IncomeDetailEntity;
import disenodesistemas.backendfunerariaapp.entities.IncomeEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.IncomeRepository;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.IncomeService;
import disenodesistemas.backendfunerariaapp.service.Interface.SupplierService;
import disenodesistemas.backendfunerariaapp.service.Interface.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final SupplierService supplierService;
    private final ProjectionFactory projectionFactory;
    private final ModelMapper modelMapper;
    private final AbstractConverter<IncomeDetailEntity, IncomeDetailRequestDto> incomeDetailConverter;

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public IncomeResponseDto createIncome(final IncomeRequestDto incomeRequestDto) {
        val incomeEntity = IncomeEntity.builder()
                .receiptNumber(incomeRequestDto.getReceiptNumber())
                .incomeUser(userService.getUserByEmail(incomeRequestDto.getIncomeUser().getEmail()))
                .receiptSeries(incomeRequestDto.getReceiptSeries())
                .receiptType(modelMapper.map(incomeRequestDto.getReceiptType(), ReceiptTypeEntity.class))
                .tax(incomeRequestDto.getTax())
                .incomeSupplier(supplierService.findSupplierEntityByNif(incomeRequestDto.getSupplier().getNif()))
                .build();
        checkExistsByByReceiptNumber(incomeRequestDto);
        if (!isEmpty(incomeRequestDto.getIncomeDetails())) {
            incomeRepository.save(incomeEntity);
            incomeEntity.setIncomeDetails(incomeDetailConverter.fromDTOs(incomeRequestDto.getIncomeDetails()));
            setItemPrices(incomeEntity.getIncomeDetails());
            incomeEntity.setTotalAmount(totalAmountCalculator(incomeEntity.getIncomeDetails(), incomeRequestDto));
        }
        return projectionFactory.createProjection(IncomeResponseDto.class, incomeRepository.save(incomeEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncomeResponseDto> getAllIncomes() {
        return incomeRepository.findAllByOrderByIdDesc();
    }

    @Override
    @Transactional
    public IncomeResponseDto updateIncome(final Long receiptNumber, final IncomeRequestDto incomeRequestDto) {
        val incomeEntity = findEntityByReceiptNumber(receiptNumber);
        if(!incomeEntity.getReceiptNumber().equals(incomeRequestDto.getReceiptNumber())) checkExistsByByReceiptNumber(incomeRequestDto);
        incomeEntity.setSupplier(supplierService.findSupplierEntityByNif(incomeRequestDto.getSupplier().getNif()));
        incomeEntity.setReceiptType(modelMapper.map(incomeRequestDto.getReceiptType(), ReceiptTypeEntity.class));
        incomeEntity.setTax(incomeRequestDto.getTax());
        incomeEntity.setReceiptSeries(incomeRequestDto.getReceiptSeries());
        incomeEntity.setReceiptNumber(incomeRequestDto.getReceiptNumber());

        incomeEntity.setLastModifiedBy(userService.getUserByEmail(incomeRequestDto.getIncomeUser().getEmail()));
        if (!isEmpty(incomeRequestDto.getIncomeDetails())) {
            final List<IncomeDetailEntity> incomeDetailEntities = getDeletedIncomeDetails(incomeEntity, incomeRequestDto);
            incomeDetailEntities.forEach(incomeEntity::removeIncomeDetail);
            incomeEntity.setIncomeDetails(incomeDetailConverter.fromDTOs(incomeRequestDto.getIncomeDetails()));
            setItemPrices(incomeEntity.getIncomeDetails());
            incomeEntity.setTotalAmount(totalAmountCalculator(incomeEntity.getIncomeDetails(), incomeRequestDto));
        }
        return projectionFactory.createProjection(IncomeResponseDto.class, incomeRepository.save(incomeEntity));
    }

    @Override
    @Transactional
    public void deleteIncome(final Long receiptNumber) {
        val incomeEntity = findEntityByReceiptNumber(receiptNumber);
        incomeRepository.delete(incomeEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncomeResponseDto> getIncomesPaginated(int page, final int limit, final String sortBy, final String sortDir) {
        if (page > 0) {
            page = page - 1;
        }
        final Pageable pageable = PageRequest.of(
                page, limit,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending()
        );
        return incomeRepository.findAllProjectedBy(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public IncomeResponseDto findByReceiptNumber(final Long receiptNumber) {
        return projectionFactory.createProjection(IncomeResponseDto.class, findEntityByReceiptNumber(receiptNumber));
    }

    private List<IncomeDetailEntity> getDeletedIncomeDetails(final IncomeEntity incomeEntity, final IncomeRequestDto incomeDto) {
        return incomeEntity.getIncomeDetails()
                .stream()
                //.filter(eDb -> !incomeDto.getIncomeDetails().contains(incomeDetailEntityToDto(eDb)))
                .filter(incomeDetailEntity -> !incomeDto.getIncomeDetails().contains(incomeDetailConverter.toDTO(incomeDetailEntity)))
                .collect(Collectors.toUnmodifiableList());
    }

    private BigDecimal totalAmountCalculator(final List<IncomeDetailEntity> incomeDetailEntities, final IncomeRequestDto incomeRequestDto) {
        //Cantidad × precio de compra de todos los detalles de ingreso y luego le sumamos el impuesto para obtener el monto total
        final BigDecimal subTotal = incomeDetailEntities.stream()
                .map(e -> e.getPurchasePrice().multiply(BigDecimal.valueOf(e.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return subTotal.add(subTotal.multiply(incomeRequestDto.getTax().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)));
    }

    private void checkExistsByByReceiptNumber(final IncomeRequestDto incomeDto) {
        if(incomeRepository.existsByReceiptNumber(incomeDto.getReceiptNumber()))
            throw new AppException("income.error.receiptNumber.already.registered", HttpStatus.CONFLICT);
    }

    private IncomeEntity findEntityByReceiptNumber(final Long receiptNumber) {
        return incomeRepository.findByReceiptNumber(receiptNumber).orElseThrow(() -> new AppException("income.error.not.found", HttpStatus.NOT_FOUND));
    }

    private void setItemPrices(final List<IncomeDetailEntity> incomeDetails) {
        final List<ItemEntity> itemsToUpdatePrice = incomeDetails.stream().filter(Objects::nonNull)
                .map(incomeDetail -> {
                    incomeDetail.getItem().setPrice(incomeDetail.getSalePrice());
                    return incomeDetail.getItem();
                }).collect(Collectors.toUnmodifiableList());
        itemRepository.saveAll(itemsToUpdatePrice);
    }

}
