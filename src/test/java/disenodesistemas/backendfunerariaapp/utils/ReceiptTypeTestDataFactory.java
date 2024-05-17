package disenodesistemas.backendfunerariaapp.utils;

import disenodesistemas.backendfunerariaapp.dto.ReceiptTypeDto;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReceiptTypeTestDataFactory {

  private static final String RECIBO_DE_CAJA = "Recibo de caja";
  private static final String INCOME_CASH_RECEIPT = "Recibo de caja de ingreso";
  private static final String EGRESS_CASH_RECEIPT = "Recibo de caja de egreso";

  public static ReceiptTypeEntity getCashReceipt() {
    final ReceiptTypeEntity receiptType = new ReceiptTypeEntity(RECIBO_DE_CAJA);
    receiptType.setId(1L);
    return receiptType;
  }

  public static ReceiptTypeDto getIncomeCashReceipt() {
    return ReceiptTypeDto.builder().id(1L).name(INCOME_CASH_RECEIPT).build();
  }

  public static ReceiptTypeDto getEgressCashReceipt() {
    return ReceiptTypeDto.builder().id(1L).name(EGRESS_CASH_RECEIPT).build();
  }

  public static ReceiptTypeEntity getEgressCashReceiptEntity() {
    final ReceiptTypeEntity receiptType = new ReceiptTypeEntity(EGRESS_CASH_RECEIPT);
    receiptType.setId(1L);
    return receiptType;
  }
}
