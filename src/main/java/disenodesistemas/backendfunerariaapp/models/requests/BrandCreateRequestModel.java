package disenodesistemas.backendfunerariaapp.models.requests;


import javax.validation.constraints.NotBlank;

public class BrandCreateRequestModel {

    @NotBlank(message = "El nombre de la marca es obligatorio.")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
