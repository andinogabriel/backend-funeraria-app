package disenodesistemas.backendfunerariaapp.models.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Getter @Setter
public class EntryRequestModel {

    @NotNull(message = "El numero de recibo es requerido.")
    private Integer receiptNumber;

    @NotNull(message = "El numero de recibo es requerido.")
    private Integer receiptSeries;

    @Digits(integer=8, fraction=2, message = "El impuesto solo debe tener 2 decimales.")
    @Positive
    private BigDecimal tax;

    @NotNull(message = "El tipo de recibo es requerido.")
    private long receiptType;

    @NotNull(message = "El proveedor es requerido.")
    private long entrySupplier;

}
