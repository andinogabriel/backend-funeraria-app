package disenodesistemas.backendfunerariaapp.models.responses;

import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class EntryRest {

    private String entryId;
    private Integer receiptSeries;
    private BigDecimal tax;
    private SupplierEntity supplierEntry;
    private ReceiptTypeEntity receiptEntry;
    private BigDecimal totalAmount;

}
