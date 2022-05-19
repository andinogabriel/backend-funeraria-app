package disenodesistemas.backendfunerariaapp.dto.response;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter @Setter @EqualsAndHashCode
public class CityDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private ProvinceDto province;

    private String name;

    private String zipCode;
}
