package disenodesistemas.backendfunerariaapp.models.responses;

import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;

import java.math.BigDecimal;

public class EntryRest {

    private String entryId;
    private Integer receiptSeries;
    private BigDecimal tax;
    private SupplierEntity supplierEntry;
    private ReceiptTypeEntity receiptEntry;
    private BigDecimal totalAmount;

}
