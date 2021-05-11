package disenodesistemas.backendfunerariaapp.security;

import disenodesistemas.backendfunerariaapp.SpringApplicationContext;

public class SecurityConstants {

    public static final long EXPIRATION_DATE = 86400000; //1 dia en milisegundos
    public static final String TOKEN_PREFIX = "Bearer "; //EN JWT siempre va primero Bearer y despues iria el token
    public static final String HEADER_STRING= "Authorization"; //Header por el cual vamos a enviar el Bearer token
    //Url por el cual los usuarios se van a registrar en el sistema
    public static final String SIGN_UP_URL = "/users";
    //Nuestra key secreta con la cual se va a generar los tokens, nuestra firma para generar los JWT
    //  https://randomkeygen.com/


    public static String getTokenSecret () {
        //Con la SpringApplicationContext podemos acceder a los bean utilizando el contexto
        AppProperties appProperties = (AppProperties) SpringApplicationContext.getBean("AppProperties");
        return appProperties.getTokenSecret();
    }

}
