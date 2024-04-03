package disenodesistemas.backendfunerariaapp.entities;

import lombok.experimental.UtilityClass;

import static org.junit.jupiter.api.Assertions.*;

@UtilityClass
public class ReceiptTypeEntityMother {

  private static final String RECIBO_DE_CAJA = "Recibo de caja";

  public static ReceiptTypeEntity getReceipt() {
    final ReceiptTypeEntity receiptType = new ReceiptTypeEntity(RECIBO_DE_CAJA);
    receiptType.setId(1L);
    return receiptType;
  }
}
