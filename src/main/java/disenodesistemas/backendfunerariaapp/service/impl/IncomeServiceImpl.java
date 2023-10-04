package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.repository.PlanRepository;
import disenodesistemas.backendfunerariaapp.service.*;
import disenodesistemas.backendfunerariaapp.service.converters.AbstractConverter;
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

import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final ItemRepository itemRepository;
    private final PlanService planService;
    private final UserService userService;
    private final SupplierService supplierService;
    private final ProjectionFactory projectionFactory;
    private final ModelMapper modelMapper;
    private final InvoiceService invoiceService;
    private final AbstractConverter<IncomeDetailEntity, IncomeDetailRequestDto> incomeDetailConverter;

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public IncomeResponseDto createIncome(final IncomeRequestDto incomeRequestDto) {
        val incomeEntity = IncomeEntity.builder()
                .receiptSeries(invoiceService.createSerialNumber())
                .receiptNumber(invoiceService.createReceiptNumber())
                .incomeUser(userService.getUserByEmail(incomeRequestDto.getIncomeUser().getEmail()))
                .receiptType(modelMapper.map(incomeRequestDto.getReceiptType(), ReceiptTypeEntity.class))
                .tax(incomeRequestDto.getTax())
                .incomeSupplier(supplierService.findSupplierEntityByNif(incomeRequestDto.getSupplier().getNif()))
                .build();
        saveIncomeDetails(incomeRequestDto, incomeEntity);
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

        incomeEntity.setSupplier(supplierService.findSupplierEntityByNif(incomeRequestDto.getSupplier().getNif()));
        incomeEntity.setReceiptType(modelMapper.map(incomeRequestDto.getReceiptType(), ReceiptTypeEntity.class));
        incomeEntity.setTax(incomeRequestDto.getTax());

        incomeEntity.setLastModifiedBy(userService.getUserByEmail(incomeRequestDto.getIncomeUser().getEmail()));
        preUpdateItemsStock(incomeEntity.getIncomeDetails());
        saveIncomeDetailsUpdated(incomeRequestDto, incomeEntity);
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

    private void saveIncomeDetails(IncomeRequestDto incomeRequestDto, IncomeEntity incomeEntity) {
        if (!isEmpty(incomeRequestDto.getIncomeDetails())) {
            incomeRepository.save(incomeEntity);
            incomeEntity.setIncomeDetails(incomeDetailConverter.fromDTOs(incomeRequestDto.getIncomeDetails()));
            setItemsPriceAndStock(incomeEntity.getIncomeDetails());
            incomeEntity.setTotalAmount(totalAmountCalculator(incomeEntity.getIncomeDetails(), incomeRequestDto));
        }
    }

    private void saveIncomeDetailsUpdated(final IncomeRequestDto incomeRequestDto, final IncomeEntity incomeEntity) {
        if (!isEmpty(incomeRequestDto.getIncomeDetails())) {
            final List<IncomeDetailEntity> incomeDetailEntities = getDeletedIncomeDetails(incomeEntity, incomeRequestDto);
            incomeDetailEntities.forEach(incomeEntity::removeIncomeDetail);
            incomeEntity.setIncomeDetails(incomeDetailConverter.fromDTOs(incomeRequestDto.getIncomeDetails()));
            setItemsPriceAndStock(incomeEntity.getIncomeDetails());
            incomeEntity.setTotalAmount(totalAmountCalculator(incomeEntity.getIncomeDetails(), incomeRequestDto));
        }
    }

    private List<IncomeDetailEntity> getDeletedIncomeDetails(final IncomeEntity incomeEntity, final IncomeRequestDto incomeDto) {
        return incomeEntity.getIncomeDetails()
                .stream()
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


    private IncomeEntity findEntityByReceiptNumber(final Long receiptNumber) {
        return incomeRepository.findByReceiptNumber(receiptNumber).orElseThrow(() -> new AppException("income.error.not.found", HttpStatus.NOT_FOUND));
    }

    private void setItemsPriceAndStock(final List<IncomeDetailEntity> incomeDetails) {
        final List<ItemEntity> itemsToUpdate = incomeDetails.stream()
                .filter(Objects::nonNull)
                .map(incomeDetail -> {
                    final ItemEntity item = incomeDetail.getItem();
                    item.setPrice(incomeDetail.getSalePrice());
                    item.setStock(nonNull(item.getStock()) ? item.getStock() + incomeDetail.getQuantity() : incomeDetail.getQuantity());
                    return item;
                })
                .collect(Collectors.toUnmodifiableList());
        itemRepository.saveAll(itemsToUpdate);
        planService.updatePlansPrice(itemsToUpdate);
    }

    private void preUpdateItemsStock(final List<IncomeDetailEntity> incomeDetails) {
        incomeDetails.forEach(incomeDetail -> incomeDetail.getItem().setStock(
                incomeDetail.getItem().getStock() - incomeDetail.getQuantity()
        ));
    }

}
