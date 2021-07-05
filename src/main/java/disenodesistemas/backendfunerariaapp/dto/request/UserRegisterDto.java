package disenodesistemas.backendfunerariaapp.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import disenodesistemas.backendfunerariaapp.security.PasswordMatches;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@PasswordMatches
@Getter @Setter
public class UserRegisterDto implements Serializable {

    private static final long serialVersionUID = 1L;

    //Clase para que el usuario se registre
    @NotBlank(message = "{user.error.lastName.blank}")
    private String lastName;

    @NotBlank(message = "{user.error.firstName.blank}")
    private String firstName;

    @NotBlank(message = "{user.error.email.blank}")
    @Pattern(regexp = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])", message = "{user.error.email.invalid}")
    private String email;

    @NotBlank(message = "{user.error.password.blank}")
    @Size(min = 8, max = 30, message = "{user.error.password.size}") //Size es para strings
    private String password;

    @NotBlank(message = "{user.error.password.blank}")
    @Size(min = 8, max = 30, message = "{user.error.password.size}")
    private String matchingPassword;

    //Por defecto crea un usuario normal
    //Si quiero un usuario Admin debo pasar este campo roles
    private Set<String> roles = new HashSet<>();

}
