package disenodesistemas.backendfunerariaapp.dto;

import disenodesistemas.backendfunerariaapp.security.SecurityConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter @Setter
public class JwtDto {

    private String authorization;
    private String email;
    private Collection<? extends GrantedAuthority> authorities;

    public JwtDto(String authorization, String email, Collection<? extends GrantedAuthority> authorities) {
        this.authorization = SecurityConstants.TOKEN_PREFIX + authorization;
        this.email = email;
        this.authorities = authorities;
    }

}
