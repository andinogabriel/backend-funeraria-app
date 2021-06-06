package disenodesistemas.backendfunerariaapp.models.requests;


import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter @Setter
public class PasswordResetRequest {


    @NotEmpty
    @Size(min = 8, max = 30, message = "La contraseña debe tener entre 8 y 30 caracteres") //Size es para strings
    private String password;

    @NotEmpty
    @Size(min = 8, max = 30, message = "La contraseña debe tener entre 8 y 30 caracteres") //Size es para strings
    private String matchingPassword;


}
