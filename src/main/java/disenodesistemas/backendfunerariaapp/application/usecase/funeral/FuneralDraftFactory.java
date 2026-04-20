package disenodesistemas.backendfunerariaapp.application.usecase.funeral;

import disenodesistemas.backendfunerariaapp.application.port.out.ReceiptNumberGeneratorPort;
import disenodesistemas.backendfunerariaapp.application.usecase.receipttype.ReceiptTypeQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.domain.entity.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.mapping.ReceiptTypeMapper;
import disenodesistemas.backendfunerariaapp.web.dto.ReceiptTypeDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.FuneralRequestDto;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FuneralDraftFactory {

  private static final BigDecimal DEFAULT_TAX = BigDecimal.valueOf(21);
  private static final String DEFAULT_RECEIPT_TYPE = "Egreso";

  private final ReceiptNumberGeneratorPort receiptNumberGeneratorPort;
  private final ReceiptTypeQueryUseCase receiptTypeQueryUseCase;
  private final ReceiptTypeMapper receiptTypeMapper;
  private final FuneralAmountCalculator funeralAmountCalculator;

  public Funeral create(
      final FuneralRequestDto funeralRequest, final Plan funeralPlan, final DeceasedEntity deceased) {
    final BigDecimal tax = resolveTax(funeralRequest.tax(), null);

    return Funeral.builder()
        .funeralDate(funeralRequest.funeralDate())
        .receiptSeries(resolveReceiptSeries(funeralRequest.receiptSeries(), null))
        .tax(tax)
        .receiptType(resolveReceiptType(funeralRequest.receiptType(), null))
        .receiptNumber(resolveReceiptNumber(funeralRequest.receiptNumber(), null))
        .plan(funeralPlan)
        .deceased(deceased)
        .totalAmount(funeralAmountCalculator.calculateTotalAmount(funeralPlan, tax))
        .build();
  }

  public void update(
      final Funeral funeral, final FuneralRequestDto funeralRequest, final Plan funeralPlan) {
    final BigDecimal tax = resolveTax(funeralRequest.tax(), funeral.getTax());

    funeral.setFuneralDate(
        funeralRequest.funeralDate() != null
            ? funeralRequest.funeralDate()
            : funeral.getFuneralDate());
    funeral.setReceiptSeries(resolveReceiptSeries(funeralRequest.receiptSeries(), funeral.getReceiptSeries()));
    funeral.setReceiptNumber(resolveReceiptNumber(funeralRequest.receiptNumber(), funeral.getReceiptNumber()));
    funeral.setTax(tax);
    funeral.setReceiptType(resolveReceiptType(funeralRequest.receiptType(), funeral.getReceiptType()));
    funeral.setPlan(funeralPlan);
    funeral.setTotalAmount(funeralAmountCalculator.calculateTotalAmount(funeralPlan, tax));
  }

  private BigDecimal resolveTax(final BigDecimal requestedTax, final BigDecimal currentTax) {
    if (requestedTax != null) {
      return requestedTax;
    }
    if (currentTax != null) {
      return currentTax;
    }
    return DEFAULT_TAX;
  }

  private String resolveReceiptSeries(final String requestedSeries, final String currentSeries) {
    if (StringUtils.isNotBlank(requestedSeries)) {
      return requestedSeries;
    }
    if (StringUtils.isNotBlank(currentSeries)) {
      return currentSeries;
    }
    return receiptNumberGeneratorPort.nextSerialNumber().toString();
  }

  private String resolveReceiptNumber(final String requestedNumber, final String currentNumber) {
    if (StringUtils.isNotBlank(requestedNumber)) {
      return requestedNumber;
    }
    if (StringUtils.isNotBlank(currentNumber)) {
      return currentNumber;
    }
    return receiptNumberGeneratorPort.nextReceiptNumber().toString();
  }

  private ReceiptTypeEntity resolveReceiptType(
      final ReceiptTypeDto requestedReceiptType, final ReceiptTypeEntity currentReceiptType) {
    if (requestedReceiptType != null) {
      return receiptTypeMapper.toEntity(requestedReceiptType);
    }
    if (currentReceiptType != null) {
      return currentReceiptType;
    }
    return receiptTypeQueryUseCase.findByNameIsContainingIgnoreCase(DEFAULT_RECEIPT_TYPE);
  }
}
