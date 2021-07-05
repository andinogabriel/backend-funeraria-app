package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Getter @Setter
public class SupplierCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{supplier.error.blank.name}")
    private String name;

    @NotBlank(message = "{supplier.error.empty.nif}")
    private String nif;

    private String webPage;

    @NotBlank(message = "{supplier.error.empty.email}")
    @Email(message = "{supplier.error.invalid.email}")
    private String email;


}
