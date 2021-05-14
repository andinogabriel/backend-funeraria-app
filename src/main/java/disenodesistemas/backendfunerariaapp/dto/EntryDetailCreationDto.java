package disenodesistemas.backendfunerariaapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter @Setter
public class EntryDetailCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private Integer quantity;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private long entry;
    private long item;

}
