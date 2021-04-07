package disenodesistemas.backendfunerariaapp.models.requests;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserLoginRequestModel {
    //Clase para que el usuario se loguee
    private String email;
    private String password;

}
