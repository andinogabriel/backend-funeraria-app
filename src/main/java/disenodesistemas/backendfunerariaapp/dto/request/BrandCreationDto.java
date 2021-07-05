package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Getter @Setter
public class BrandCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{brand.error.blank.name}")
    private String name;

    private String webPage;

}
