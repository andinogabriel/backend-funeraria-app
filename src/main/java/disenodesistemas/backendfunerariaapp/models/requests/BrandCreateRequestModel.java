package disenodesistemas.backendfunerariaapp.models.requests;


import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter @Setter
public class BrandCreateRequestModel {

    @NotBlank(message = "El nombre de la marca es obligatorio.")
    private String name;

    private String webPage;

}
