package disenodesistemas.backendfunerariaapp.dto.response;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter @Setter @EqualsAndHashCode @ToString
public class ProvinceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String code31662;

    private String name;
}
