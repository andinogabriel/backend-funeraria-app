package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter @Setter
public class EntryCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "{entry.error.empty.receiptNumber}")
    private Integer receiptNumber;

    @NotNull(message = "{entry.error.empty.receiptSeries}")
    private Integer receiptSeries;

    @NotNull(message = "{entry.error.empty.tax}")
    @Digits(integer=8, fraction=2, message = "{entry.error.digits.tax}")
    @DecimalMin(value = "0.00", inclusive = false, message = "{entry.error.min.tax}")
    @DecimalMax(value = "100.00", message = "{entry.error.max.tax }")
    private BigDecimal tax;

    @NotNull(message = "{entry.error.empty.receiptType}")
    private long receiptType;

    @NotNull(message = "{entry.error.empty.entrySupplier}")
    private long entrySupplier;

    private String entryUser;
}