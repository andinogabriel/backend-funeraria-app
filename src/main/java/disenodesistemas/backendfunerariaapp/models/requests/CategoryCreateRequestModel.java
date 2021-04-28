package disenodesistemas.backendfunerariaapp.models.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter @Setter
public class CategoryCreateRequestModel {

    @NotBlank(message = "El nombre es requerido.")
    private String name;

    private String description;

}
