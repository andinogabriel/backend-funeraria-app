package disenodesistemas.backendfunerariaapp.models.responses;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class ItemRest {

    private long id;
    private String name;
    private String description;
    private String code;
    private String itemImageLink;
    private BigDecimal price;
    private BigDecimal itemLength;
    private BigDecimal itemHeight;
    private BigDecimal itemWidth;
    private BrandRest brand;
    private CategoryRest category;

}
