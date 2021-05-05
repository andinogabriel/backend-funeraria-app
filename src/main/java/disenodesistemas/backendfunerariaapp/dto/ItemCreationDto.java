package disenodesistemas.backendfunerariaapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter @Setter
public class ItemCreationDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private String code;
    private String itemImageLink;
    private BigDecimal price;
    private BigDecimal itemLength;
    private BigDecimal itemHeight;
    private BigDecimal itemWidth;
    private long brand;
    private long category;
}
