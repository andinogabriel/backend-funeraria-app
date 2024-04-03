package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.FuneralRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.FuneralResponseDto;
import disenodesistemas.backendfunerariaapp.entities.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.entities.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.entities.Funeral;
import disenodesistemas.backendfunerariaapp.entities.Plan;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.exceptions.ConflictException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.AffiliateRepository;
import disenodesistemas.backendfunerariaapp.repository.DeceasedRepository;
import disenodesistemas.backendfunerariaapp.repository.FuneralRepository;
import disenodesistemas.backendfunerariaapp.service.FuneralService;
import disenodesistemas.backendfunerariaapp.service.InvoiceService;
import disenodesistemas.backendfunerariaapp.service.PlanService;
import disenodesistemas.backendfunerariaapp.service.ReceiptTypeService;
import disenodesistemas.backendfunerariaapp.service.converters.AbstractConverter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FuneralServiceImpl implements FuneralService {

  private final FuneralRepository funeralRepository;
  private final DeceasedRepository deceasedRepository;
  private final AffiliateRepository affiliateRepository;
  private final PlanService planService;
  private final ProjectionFactory projectionFactory;
  private final ModelMapper modelMapper;
  private final InvoiceService invoiceService;
  private final ReceiptTypeService receiptTypeService;
  private final AbstractConverter<DeceasedEntity, DeceasedRequestDto> deceasedConverter;
  private static final BigDecimal DEFAULT_TAX = BigDecimal.valueOf(21);
  private static final String DEFAULT_RECEIPT_TYPE = "Egreso";

  @Override
  @Transactional
  public FuneralResponseDto create(final FuneralRequestDto funeralRequest) {
    validateReceiptNumber(funeralRequest.getReceiptNumber());
    validateDeceasedDniRequest(funeralRequest.getDeceased().getDni());
    final Plan funeralPlan = planService.findById(funeralRequest.getPlan().getId());
    final Optional<AffiliateEntity> affiliateEntityOptional =
        affiliateRepository.findByDni(funeralRequest.getDeceased().getDni());
    affiliateEntityOptional.ifPresent(
        affiliateEntity -> {
          affiliateEntity.setDeceased(Boolean.TRUE);
          affiliateRepository.save(affiliateEntity);
        });
    final DeceasedEntity deceased =
        saveDeceased(funeralRequest.getDeceased(), affiliateEntityOptional.isPresent());
    final BigDecimal tax = ObjectUtils.defaultIfNull(funeralRequest.getTax(), DEFAULT_TAX);
    final Funeral funeral = buildFuneral(funeralRequest, funeralPlan, deceased, tax);
    funeral.setDeceased(deceased);
    final Funeral savedFuneral = funeralRepository.save(funeral);
    return projectionFactory.createProjection(FuneralResponseDto.class, savedFuneral);
  }

  @Override
  @Transactional
  public FuneralResponseDto update(final Long id, final FuneralRequestDto funeralRequest) {
    final Funeral funeralToUpdate = findEntityById(id);
    validateUniqueReceiptNumber(
        funeralRequest.getReceiptNumber(), funeralToUpdate.getReceiptNumber());
    validateDeceasedDniRequest(funeralRequest.getDeceased().getDni());
    funeralToUpdate.setFuneralDate(funeralToUpdate.getFuneralDate());
    funeralToUpdate.setReceiptSeries(funeralRequest.getReceiptSeries());
    final BigDecimal tax = ObjectUtils.defaultIfNull(funeralRequest.getTax(), DEFAULT_TAX);
    funeralToUpdate.setTax(tax);
    funeralToUpdate.setTotalAmount(getTotalAmount(funeralToUpdate.getPlan(), tax));
    return projectionFactory.createProjection(
        FuneralResponseDto.class, funeralRepository.save(funeralToUpdate));
  }

  @Override
  @Transactional
  public void delete(final Long id) {
    funeralRepository.delete(findEntityById(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<FuneralResponseDto> findAll() {
    return funeralRepository.findAllByOrderByRegisterDateDesc();
  }

  @Override
  @Transactional(readOnly = true)
  public FuneralResponseDto findById(final Long id) {
    return projectionFactory.createProjection(FuneralResponseDto.class, findEntityById(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<FuneralResponseDto> findFuneralsByUser() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return funeralRepository.findFuneralsByUserEmail(authentication.getName());
  }

  private Funeral findEntityById(final Long id) {
    return funeralRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("funeral.error.not.found"));
  }

  private void validateReceiptNumber(final String receiptNumber) {
    if (existsByReceiptNumber(receiptNumber))
      throw new ConflictException("funeral.error.receiptNumber.already.exists");
  }

  private void validateUniqueReceiptNumber(
      final String newReceiptNumber, final String oldReceiptNumber) {
    if (existsByReceiptNumber(newReceiptNumber)
        && !Objects.equals(newReceiptNumber, oldReceiptNumber)) {
      throw new ConflictException("funeral.error.receiptNumber.already.exists");
    }
  }

  private void validateDeceasedDniRequest(final Integer dni) {
    if (deceasedRepository.existsByDni(dni))
      throw new ConflictException("funeral.error.deceased.dni.already.exists");
  }

  private DeceasedEntity saveDeceased(
      final DeceasedRequestDto deceasedRequest, boolean affiliated) {
    final DeceasedEntity deceased = deceasedConverter.fromDto(deceasedRequest);
    deceased.setAffiliated(affiliated);
    return deceasedRepository.save(deceased);
  }

  private BigDecimal getTotalAmount(final Plan funeralPlan, final BigDecimal tax) {
    final BigDecimal taxDecimal = tax.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    final BigDecimal priceWithTax = funeralPlan.getPrice().multiply(BigDecimal.ONE.add(taxDecimal));
    return priceWithTax.setScale(2, RoundingMode.HALF_UP);
  }

  private boolean existsByReceiptNumber(final String receiptNumber) {
    return funeralRepository.existsByReceiptNumber(receiptNumber);
  }

  private Funeral buildFuneral(
      final FuneralRequestDto funeralRequest,
      final Plan funeralPlan,
      final DeceasedEntity deceased,
      final BigDecimal tax) {
    return Funeral.builder()
        .funeralDate(funeralRequest.getFuneralDate())
        .receiptSeries(
            ObjectUtils.defaultIfNull(
                invoiceService.createSerialNumber().toString(), funeralRequest.getReceiptSeries()))
        .tax(tax)
        .receiptType(
            ObjectUtils.defaultIfNull(
                receiptTypeService.findByNameIsContainingIgnoreCase(DEFAULT_RECEIPT_TYPE),
                modelMapper.map(funeralRequest.getReceiptType(), ReceiptTypeEntity.class)))
        .receiptNumber(
            ObjectUtils.defaultIfNull(
                invoiceService.createReceiptNumber().toString(), funeralRequest.getReceiptNumber()))
        .plan(funeralPlan)
        .deceased(deceased)
        .totalAmount(getTotalAmount(funeralPlan, tax))
        .build();
  }
}
