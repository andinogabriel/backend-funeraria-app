package disenodesistemas.backendfunerariaapp.models.responses;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter @Setter
public class EntryRest {

    private long id;
    private Integer receiptNumber;
    private Integer receiptSeries;
    private Date entryDate;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private SupplierRest entrySupplier;
    private ReceiptTypeRest receiptType;
    private UserRest entryUser;
    private List<EntryDetailRest> entryDetails = new ArrayList<>();


}
