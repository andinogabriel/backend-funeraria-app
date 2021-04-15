package disenodesistemas.backendfunerariaapp.models.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter @Setter
public class AffiliateDetailsRequestModel {

    @NotNull(message = "El nombre del afiliado es obligatorio.")
    private String firstName;

    @NotNull(message = "El nombre del afiliado es obligatorio.")
    private String lastName;

    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date birthDate;

    @NotNull(message = "El número de DNI es obligatorio.")
    private int dni;

    @NotNull(message = "El parentesco es obligatorio.")
    @Range(min = 1, max = 31, message = "El parentesco es invalido.")
    private long affiliateRelationship;

    @NotNull(message = "El genero es obligatorio.")
    @Range(min = 1, max = 3, message = "El genero es invalido.")
    private long genderId;


}
