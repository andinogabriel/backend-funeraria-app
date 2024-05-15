package disenodesistemas.backendfunerariaapp.service.impl;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import disenodesistemas.backendfunerariaapp.dto.request.IncomeDetailRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.IncomeResponseDto;
import disenodesistemas.backendfunerariaapp.entities.IncomeDetailEntity;
import disenodesistemas.backendfunerariaapp.entities.IncomeEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.IncomeRepository;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import disenodesistemas.backendfunerariaapp.service.EntityProcessor;
import disenodesistemas.backendfunerariaapp.service.IncomeService;
import disenodesistemas.backendfunerariaapp.service.InvoiceService;
import disenodesistemas.backendfunerariaapp.service.PlanService;
import disenodesistemas.backendfunerariaapp.service.SupplierService;
import disenodesistemas.backendfunerariaapp.service.UserService;
import disenodesistemas.backendfunerariaapp.service.converters.AbstractConverter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
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
  private final EntityProcessor<IncomeDetailEntity, IncomeDetailRequestDto>
      incomeDetailEntityProcessor;

  public IncomeServiceImpl(
      final IncomeRepository incomeRepository,
      final ItemRepository itemRepository,
      final PlanService planService,
      final UserService userService,
      final SupplierService supplierService,
      final ProjectionFactory projectionFactory,
      final ModelMapper modelMapper,
      final InvoiceService invoiceService,
      final AbstractConverter<IncomeDetailEntity, IncomeDetailRequestDto> incomeDetailConverter) {
    this.incomeRepository = incomeRepository;
    this.itemRepository = itemRepository;
    this.planService = planService;
    this.userService = userService;
    this.supplierService = supplierService;
    this.projectionFactory = projectionFactory;
    this.modelMapper = modelMapper;
    this.invoiceService = invoiceService;
    this.incomeDetailConverter = incomeDetailConverter;
    this.incomeDetailEntityProcessor = new DefaultEntityProcessor<>();
  }

  @Override
  @Transactional(propagation = Propagation.SUPPORTS)
  public IncomeResponseDto create(final IncomeRequestDto incomeRequestDto) {
    val incomeEntity =
        IncomeEntity.builder()
            .receiptSeries(invoiceService.createSerialNumber())
            .receiptNumber(invoiceService.createReceiptNumber())
            .incomeUser(userService.getUserByEmail(incomeRequestDto.getIncomeUser().getEmail()))
            .receiptType(
                modelMapper.map(incomeRequestDto.getReceiptType(), ReceiptTypeEntity.class))
            .tax(incomeRequestDto.getTax())
            .incomeSupplier(
                supplierService.findSupplierEntityByNif(incomeRequestDto.getSupplier().getNif()))
            .build();
    saveIncomeDetails(incomeRequestDto, incomeEntity);
    return projectionFactory.createProjection(
        IncomeResponseDto.class, incomeRepository.save(incomeEntity));
  }

  @Override
  @Transactional(readOnly = true)
  public List<IncomeResponseDto> findAll() {
    return incomeRepository.findAllByOrderByIdDesc();
  }

  @Override
  @Transactional(readOnly = true)
  public IncomeResponseDto findById(final Long receiptNumber) {
    return projectionFactory.createProjection(
        IncomeResponseDto.class, findEntityByReceiptNumber(receiptNumber));
  }

  @Override
  @Transactional
  public IncomeResponseDto update(
      final Long receiptNumber, final IncomeRequestDto incomeRequestDto) {
    val incomeEntity = findEntityByReceiptNumber(receiptNumber);

    incomeEntity.setSupplier(
        supplierService.findSupplierEntityByNif(incomeRequestDto.getSupplier().getNif()));
    incomeEntity.setReceiptType(
        modelMapper.map(incomeRequestDto.getReceiptType(), ReceiptTypeEntity.class));
    incomeEntity.setTax(incomeRequestDto.getTax());

    incomeEntity.setLastModifiedBy(
        userService.getUserByEmail(incomeRequestDto.getIncomeUser().getEmail()));
    preUpdateItemsStock(incomeEntity.getIncomeDetails());
    saveIncomeDetailsUpdated(incomeRequestDto, incomeEntity);
    return projectionFactory.createProjection(
        IncomeResponseDto.class, incomeRepository.save(incomeEntity));
  }

  @Override
  @Transactional
  public void delete(final Long receiptNumber) {
    val incomeEntity = findEntityByReceiptNumber(receiptNumber);
    incomeRepository.delete(incomeEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<IncomeResponseDto> getIncomesPaginated(
      int page, final int limit, final String sortBy, final String sortDir) {
    if (page > 0) {
      page = page - 1;
    }
    final Pageable pageable =
        PageRequest.of(
            page,
            limit,
            sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending());
    return incomeRepository.findAllProjectedBy(pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public IncomeResponseDto findByReceiptNumber(final Long receiptNumber) {
    return projectionFactory.createProjection(
        IncomeResponseDto.class, findEntityByReceiptNumber(receiptNumber));
  }

  private void saveIncomeDetails(
      final IncomeRequestDto incomeRequestDto, final IncomeEntity incomeEntity) {
    if (isNotEmpty(incomeRequestDto.getIncomeDetails())) {
      incomeRepository.save(incomeEntity);
      incomeEntity.setIncomeDetails(
          incomeDetailConverter.fromDTOs(incomeRequestDto.getIncomeDetails()));
      setItemsPriceAndStock(incomeEntity.getIncomeDetails());
      incomeEntity.setTotalAmount(
          totalAmountCalculator(incomeEntity.getIncomeDetails(), incomeRequestDto));
    }
  }

  private void saveIncomeDetailsUpdated(
      final IncomeRequestDto incomeRequestDto, final IncomeEntity incomeEntity) {
    if (isNotEmpty(incomeRequestDto.getIncomeDetails())) {
      final Function<IncomeDetailEntity, IncomeDetailRequestDto> entityToDtoConverter =
          incomeDetailConverter::toDTO;
      final List<IncomeDetailEntity> incomeDetailEntities =
          incomeDetailEntityProcessor.getDeletedEntities(
              incomeEntity.getIncomeDetails(),
              incomeRequestDto.getIncomeDetails(),
              entityToDtoConverter);
      incomeDetailEntities.forEach(incomeEntity::removeIncomeDetail);
      incomeEntity.setIncomeDetails(
          incomeDetailConverter.fromDTOs(incomeRequestDto.getIncomeDetails()));
      setItemsPriceAndStock(incomeEntity.getIncomeDetails());
      incomeEntity.setTotalAmount(
          totalAmountCalculator(incomeEntity.getIncomeDetails(), incomeRequestDto));
    }
  }

  private BigDecimal totalAmountCalculator(
      final List<IncomeDetailEntity> incomeDetailEntities,
      final IncomeRequestDto incomeRequestDto) {
    final BigDecimal subTotal =
        incomeDetailEntities.stream()
            .filter(Objects::nonNull)
            .map(
                detail ->
                    detail.getPurchasePrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    final BigDecimal taxAmount =
        subTotal.multiply(
            incomeRequestDto.getTax().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
    return subTotal.add(taxAmount);
  }

  private IncomeEntity findEntityByReceiptNumber(final Long receiptNumber) {
    return incomeRepository
        .findByReceiptNumber(receiptNumber)
        .orElseThrow(() -> new NotFoundException("income.error.not.found"));
  }

  private void setItemsPriceAndStock(final List<IncomeDetailEntity> incomeDetails) {
    if (incomeDetails.isEmpty()) return;

    final List<ItemEntity> itemsToUpdate =
        incomeDetails.stream()
            .filter(Objects::nonNull)
            .map(
                incomeDetail -> {
                  final ItemEntity item = incomeDetail.getItem();
                  item.setPrice(incomeDetail.getSalePrice());
                  item.setStock(
                      Optional.ofNullable(item.getStock())
                          .map(stock -> stock + incomeDetail.getQuantity())
                          .orElse(incomeDetail.getQuantity()));
                  return item;
                })
            .collect(Collectors.toUnmodifiableList());
    itemRepository.saveAll(itemsToUpdate);
    planService.updatePlansPrice(itemsToUpdate);
  }

  private void preUpdateItemsStock(final List<IncomeDetailEntity> incomeDetails) {
    incomeDetails.forEach(
        incomeDetail ->
            incomeDetail
                .getItem()
                .setStock(incomeDetail.getItem().getStock() - incomeDetail.getQuantity()));
  }
}
