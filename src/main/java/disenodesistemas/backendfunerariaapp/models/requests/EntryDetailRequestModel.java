package disenodesistemas.backendfunerariaapp.models.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Getter @Setter
public class EntryDetailRequestModel {

    @NotNull(message = "La cantidad es requerida.")
    private Integer quantity;

    @NotNull(message = "El precio de compra es requerido.")
    @Digits(integer=8, fraction=2, message = "El precio de compra solo debe tener 2 decimales.")
    @Positive
    private BigDecimal purchasePrice;

    @NotNull(message = "El precio de venta es requerido.")
    @Digits(integer=8, fraction=2, message = "El precio de venta solo debe tener 2 decimales.")
    @Positive
    private BigDecimal salePrice;

    @NotNull(message = "El ingreso es requerido.")
    private long entry;

    @NotNull(message = "El articulo es requerido.")
    private long item;

}
