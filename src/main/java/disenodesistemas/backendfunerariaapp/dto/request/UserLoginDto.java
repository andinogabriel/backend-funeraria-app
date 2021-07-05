package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Getter @Setter
public class UserLoginDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{user.error.email.blank}")
    @Email(message = "{user.error.email.invalid }")
    private String email;

    @NotBlank(message = "{user.error.password.blank}")
    private String password;

}
