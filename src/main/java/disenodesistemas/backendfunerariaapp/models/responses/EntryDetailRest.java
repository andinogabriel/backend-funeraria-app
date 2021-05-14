package disenodesistemas.backendfunerariaapp.models.responses;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class EntryDetailRest {

    private Integer quantity;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private ItemRest item;

}
