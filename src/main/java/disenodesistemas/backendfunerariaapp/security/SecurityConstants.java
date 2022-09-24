package disenodesistemas.backendfunerariaapp.security;

import disenodesistemas.backendfunerariaapp.SpringApplicationContext;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityConstants {

    public static final long EXPIRATION_DATE = 86400000; //1 dia en milisegundos
    public static final String TOKEN_PREFIX = "Bearer "; //EN JWT siempre va primero Bearer y despues iria el token
    public static final String AUTHORITIES = "authorities";
    public static final String HEADER_STRING= "Authorization"; //Header por el cual vamos a enviar el Bearer token
    //Nuestra key secreta con la cual se va a generar los tokens, nuestra firma para generar los JWT
    //  https://randomkeygen.com/


    public static String getTokenSecret () {
        //Con la SpringApplicationContext podemos acceder a los bean utilizando el contexto
        final AppProperties appProperties = (AppProperties) SpringApplicationContext.getBean("AppProperties");
        return appProperties.getTokenSecret();
    }

}
