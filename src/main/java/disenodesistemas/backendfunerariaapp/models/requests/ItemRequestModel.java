package disenodesistemas.backendfunerariaapp.models.requests;

import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter @Getter
public class ItemRequestModel {

    private String name;
    private String description;
    private String image;
    private BigDecimal price;
    private CategoryEntity itemCategory;

}
