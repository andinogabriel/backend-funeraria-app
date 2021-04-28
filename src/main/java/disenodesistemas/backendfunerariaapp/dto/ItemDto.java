package disenodesistemas.backendfunerariaapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
public class ItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private String description;
    private String code;
    private String image;
    private BigDecimal price;
    private BigDecimal itemLength;
    private BigDecimal itemHeight;
    private BigDecimal itemWidth;
    private BrandDto brand;
    private CategoryDto category;
    private List<EntryDetailDto> entryDetails;
    private List<ServiceDetailDto> serviceDetails;


}
