package disenodesistemas.backendfunerariaapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Getter @Setter
public class EntryCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private Integer receiptNumber;
    private Integer receiptSeries;
    private Date entryDate;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private long receiptType;
    private long entrySupplier;
    private String entryUser;
    private List<EntryDetailCreationDto> entryDetails = new ArrayList<>();


}
