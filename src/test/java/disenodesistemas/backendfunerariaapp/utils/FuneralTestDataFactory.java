package disenodesistemas.backendfunerariaapp.utils;

import static disenodesistemas.backendfunerariaapp.utils.DeceasedTestDataFactory.getAffiliatedDeceasedRequestDto;
import static disenodesistemas.backendfunerariaapp.utils.DeceasedTestDataFactory.getDeceasedExistingDniRequestDto;
import static disenodesistemas.backendfunerariaapp.utils.DeceasedTestDataFactory.getDeceasedRequestDto;
import static disenodesistemas.backendfunerariaapp.utils.DeceasedTestDataFactory.getSavedInDbDeceasedRequestDto;
import static disenodesistemas.backendfunerariaapp.utils.PlanTestDataFactory.getExistingPlanRequest;
import static disenodesistemas.backendfunerariaapp.utils.ReceiptTypeTestDataFactory.getEgressCashReceipt;

import disenodesistemas.backendfunerariaapp.dto.request.FuneralRequestDto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FuneralTestDataFactory {

  private static final BigDecimal TAX = new BigDecimal("21.00");
  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final LocalDateTime FUNERAL_DATE =
      LocalDateTime.parse("2024-11-11 03:00:00", FORMATTER);

  public static FuneralRequestDto getFuneralRequestDto() {
    return FuneralRequestDto.builder()
        .funeralDate(FUNERAL_DATE)
        .tax(TAX)
        .receiptType(getEgressCashReceipt())
        .deceased(getDeceasedRequestDto())
        .plan(getExistingPlanRequest())
        .build();
  }

  public static FuneralRequestDto getSavedInDBFuneralRequestDto() {
    return FuneralRequestDto.builder()
        .receiptNumber("123465sad465")
        .receiptSeries("465asd4as")
        .funeralDate(FUNERAL_DATE)
        .tax(TAX)
        .receiptType(getEgressCashReceipt())
        .deceased(getSavedInDbDeceasedRequestDto())
        .plan(getExistingPlanRequest())
        .build();
  }

  public static FuneralRequestDto getSavedInDBFuneralRequestDtoThrowsException() {
    return FuneralRequestDto.builder()
        .receiptNumber("2024290420241A")
        .receiptSeries("465asd4as")
        .funeralDate(FUNERAL_DATE)
        .tax(TAX)
        .receiptType(getEgressCashReceipt())
        .deceased(getSavedInDbDeceasedRequestDto())
        .plan(getExistingPlanRequest())
        .build();
  }

  public static FuneralRequestDto getFuneralRequestDtoAffiliatedDeceased() {
    return FuneralRequestDto.builder()
        .funeralDate(FUNERAL_DATE)
        .tax(TAX)
        .receiptType(getEgressCashReceipt())
        .deceased(getAffiliatedDeceasedRequestDto())
        .plan(getExistingPlanRequest())
        .build();
  }

  public static FuneralRequestDto getFuneralExistingReceiptNumberRequestDto() {
    return FuneralRequestDto.builder()
        .receiptNumber("123465sad465")
        .funeralDate(FUNERAL_DATE)
        .tax(TAX)
        .receiptType(getEgressCashReceipt())
        .deceased(getDeceasedRequestDto())
        .plan(getExistingPlanRequest())
        .build();
  }

  public static FuneralRequestDto getFuneralExistingDeceasedDniRequestDto() {
    return FuneralRequestDto.builder()
        .funeralDate(FUNERAL_DATE)
        .tax(TAX)
        .receiptType(getEgressCashReceipt())
        .deceased(getDeceasedExistingDniRequestDto())
        .plan(getExistingPlanRequest())
        .build();
  }
}
