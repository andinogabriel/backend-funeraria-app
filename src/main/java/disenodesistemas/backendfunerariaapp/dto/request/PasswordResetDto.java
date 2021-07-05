package disenodesistemas.backendfunerariaapp.dto.request;


import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter @Setter
public class PasswordResetDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{password.error.blank.field}")
    @Size(min = 8, max = 30, message = "{password.error.size.field}") //Size es para strings
    private String password;

    @NotBlank(message = "{password.error.blank.field}")
    @Size(min = 8, max = 30, message = "{password.error.size.field}") //Size es para strings
    private String matchingPassword;

}
