package disenodesistemas.backendfunerariaapp.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

//Esta clase es para poder leer application.properties
@Component
@ConfigurationProperties("app")
@Getter @Setter
public class AppProperties {
    private String tokenSecret;
}
