package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.IncomeDetailRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.IncomeResponseDto;
import disenodesistemas.backendfunerariaapp.entities.IncomeDetailEntity;
import disenodesistemas.backendfunerariaapp.entities.IncomeEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
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
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
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

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public IncomeResponseDto createIncome(final IncomeRequestDto incomeRequestDto) {
        val incomeEntity = IncomeEntity.builder()
                .incomeUser(userService.getUserByEmail(incomeRequestDto.getIncomeUser().getEmail()))
                .receiptSeries(incomeRequestDto.getReceiptSeries())
                .receiptType(modelMapper.map(incomeRequestDto.getReceiptType(), ReceiptTypeEntity.class))
                .tax(incomeRequestDto.getTax())
                .incomeSupplier(supplierService.findSupplierEntityByNif(incomeRequestDto.getSupplier().getNif()))
                .build();
        checkExistsByByReceiptNumber(incomeEntity, incomeRequestDto);
        if (!isEmpty(incomeRequestDto.getIncomeDetails())) {
            incomeRepository.save(incomeEntity);
            deleteIncomeWhereIsDeletedTrue(incomeEntity);
            incomeEntity.setIncomeDetails(getIncomeDetailsEntities(incomeRequestDto.getIncomeDetails()));
            setItemPrices(incomeEntity.getIncomeDetails());
        }
        totalAmountCalculator(incomeEntity, incomeRequestDto);
        return projectionFactory.createProjection(IncomeResponseDto.class, incomeRepository.save(incomeEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncomeResponseDto> getAllIncomes(final Boolean deleted) {
        return Boolean.TRUE.equals(deleted) ? incomeRepository.findAllByOrderByIdDesc()
                : incomeRepository.findByDeletedFalseOrderByIdDesc();
    }

    @Override
    @Transactional
    public IncomeResponseDto updateIncome(final Long receiptNumber, final IncomeRequestDto incomeRequestDto) {
        val incomeEntity = findEntityByReceiptNumber(receiptNumber);
        if(!incomeEntity.getReceiptNumber().equals(incomeRequestDto.getReceiptNumber())) checkExistsByByReceiptNumber(incomeEntity, incomeRequestDto);
        incomeEntity.setSupplier(modelMapper.map(incomeRequestDto.getSupplier(), SupplierEntity.class));
        incomeEntity.setReceiptType(modelMapper.map(incomeRequestDto.getReceiptType(), ReceiptTypeEntity.class));
        incomeEntity.setTax(incomeRequestDto.getTax());
        incomeEntity.setReceiptSeries(incomeRequestDto.getReceiptSeries());

        totalAmountCalculator(incomeEntity, incomeRequestDto);
        incomeEntity.setLastModifiedBy(modelMapper.map(incomeRequestDto.getIncomeUser(), UserEntity.class));
        if (!isEmpty(incomeRequestDto.getIncomeDetails())) {
            final List<IncomeDetailEntity> incomeDetailEntities = getDeletedIncomeDetails(incomeEntity, incomeRequestDto);
            incomeDetailEntities.forEach(incomeEntity::removeIncomeDetail);
            incomeEntity.setIncomeDetails(modelMapper.map(incomeRequestDto.getIncomeDetails(), new TypeToken<List<IncomeDetailEntity>>() {}.getType()));
            deleteIncomeWhereIsDeletedTrue(incomeEntity);
            setItemPrices(incomeEntity.getIncomeDetails());
        }
        return projectionFactory.createProjection(IncomeResponseDto.class, incomeRepository.save(incomeEntity));
    }

    @Override
    @Transactional
    public void deleteIncome(final Long receiptNumber) {
        val incomeEntity = findEntityByReceiptNumber(receiptNumber);
        if (incomeEntity.isDeleted()) {
            incomeRepository.delete(incomeEntity);
            throw new AppException("income.error.already.deleted", HttpStatus.CONFLICT);
        }
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String email = authentication.getName();
        incomeEntity.setLastModifiedBy(userService.getUserByEmail(email));
        incomeRepository.save(incomeEntity);
        incomeRepository.deleteByReceiptNumber(incomeEntity.getReceiptNumber());
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
                .filter(eDb -> !incomeDto.getIncomeDetails().contains(modelMapper.map(eDb, IncomeDetailRequestDto.class)))
                .collect(Collectors.toUnmodifiableList());
    }

    private void totalAmountCalculator(final IncomeEntity incomeEntity, final IncomeRequestDto incomeRequestDto) {
        incomeEntity.setTotalAmount(BigDecimal.valueOf(0));
        //Cantidad × precio de compra de todos los detalles de ingreso y luego le sumamos el impuesto para obtener el monto total
        final BigDecimal subTotal = incomeEntity.getIncomeDetails().stream()
                .map(e -> e.getPurchasePrice().multiply(BigDecimal.valueOf(e.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        final BigDecimal total = subTotal.add(subTotal.multiply(incomeRequestDto.getTax().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)));
        incomeEntity.setTotalAmount(new BigDecimal(total.toPlainString()).setScale(2, RoundingMode.HALF_UP));
    }

    private void checkExistsByByReceiptNumber(final IncomeEntity incomeEntity, final IncomeRequestDto incomeDto) {
        final boolean isDeleted = incomeRepository.findByReceiptNumber(incomeDto.getReceiptNumber())
                .map(IncomeEntity::isDeleted).orElse(true);
        if(!isDeleted)
            throw new AppException("income.error.receiptNumber.already.registered", HttpStatus.CONFLICT);
        incomeEntity.setReceiptNumber(incomeDto.getReceiptNumber());
    }

    private IncomeEntity findEntityByReceiptNumber(final Long receiptNumber) {
        return incomeRepository.findByReceiptNumber(receiptNumber).orElseThrow(() -> new AppException("income.error.not.found", HttpStatus.NOT_FOUND));
    }

    private List<IncomeDetailEntity> getIncomeDetailsEntities(final List<IncomeDetailRequestDto> incomeDetailsRequestDto) {
        final List<ItemEntity> itemEntities = (List<ItemEntity>) itemRepository.findAll();
        return incomeDetailsRequestDto.stream().map(
                income -> {
                    final ItemEntity itemEntity = itemEntities.stream()
                            .filter(item -> income.getItem().getCode().equals(item.getCode()))
                            .findFirst().orElse(null);
                    final IncomeDetailEntity incomeToReturn = modelMapper.map(income, IncomeDetailEntity.class);
                    incomeToReturn.setItem(itemEntity);
                    return incomeToReturn;
                }
        ).collect(Collectors.toUnmodifiableList());
    }

    private void setItemPrices(final List<IncomeDetailEntity> incomeDetails) {
        final List<ItemEntity> itemsToUpdatePrice = incomeDetails.stream().filter(Objects::nonNull)
                .map(incomeDetail -> {
                    incomeDetail.getItem().setPrice(incomeDetail.getSalePrice());
                    return incomeDetail.getItem();
                }).collect(Collectors.toUnmodifiableList());
        itemRepository.saveAll(itemsToUpdatePrice);
    }

    private void deleteIncomeWhereIsDeletedTrue(final IncomeEntity incomeEntity) {
        final IncomeEntity incomeEntityToDelete = incomeRepository.findByReceiptNumberOrderByIdDesc(incomeEntity.getReceiptNumber())
                .stream()
                .filter(IncomeEntity::isDeleted)
                .findFirst().orElse(null);
        if (nonNull(incomeEntity)) incomeRepository.delete(incomeEntityToDelete);
    }

}
