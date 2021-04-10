package disenodesistemas.backendfunerariaapp.models.requests;

import disenodesistemas.backendfunerariaapp.entities.EntryDetailEntity;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
public class EntryRequestModel {

    private Integer receiptNumber;
    private Integer receiptSeries;
    private BigDecimal tax;
    private SupplierEntity supplierEntry;
    private ReceiptTypeEntity receiptEntry;



}
