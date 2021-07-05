package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter @Setter
public class EntryDetailCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "{entryDetail.error.empty.quantity}")
    private Integer quantity;

    @NotNull(message = "{entryDetail.error.empty.purchasePrice}")
    @Digits(integer=8, fraction=2, message = "{entryDetail.error.digits.purchasePrice}")
    @Positive(message = "{entryDetail.error.positive.purchasePrice}")
    private BigDecimal purchasePrice;

    @NotNull(message = "{entryDetail.error.empty.salePrice}")
    @Digits(integer=8, fraction=2, message = "{entryDetail.error.digits.salePrice}")
    @Positive(message = "{entryDetail.error.positive.salePrice}")
    private BigDecimal salePrice;

    @NotNull(message = "{entryDetail.error.null.entry}")
    private long entry;

    @NotNull(message = "{entryDetail.error.null.item}")
    private long item;

}
