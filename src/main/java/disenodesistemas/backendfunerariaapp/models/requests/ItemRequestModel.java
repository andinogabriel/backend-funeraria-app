package disenodesistemas.backendfunerariaapp.models.requests;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Setter @Getter
public class ItemRequestModel {

    @NotBlank(message = "El nombre es obligatorio.")
    private String name;

    private String description;

    private long brand;

    private String code;

    private String itemImageLink;

    @Digits(integer=8, fraction=2, message = "El precio solo debe tener 2 decimales.")
    @Positive
    private BigDecimal price;

    @Digits(integer=8, fraction=2, message = "La longitud del ataúd solo debe tener 2 decimales.")
    @Positive
    private BigDecimal itemLength;

    @Digits(integer=8, fraction=2, message = "La altura del ataúd solo debe tener 2 decimales.")
    @Positive
    private BigDecimal itemHeight;

    @Digits(integer=8, fraction=2, message = "El ancho del ataúd solo debe tener 2 decimales.")
    @Positive
    private BigDecimal itemWidth;

    @NotNull(message = "La categoria es requerida.")
    private long category;



}
