package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

//Clase Encargada de generar la seguridad. Implementa los privilegios de cada usuario. UserDetails es una clase propia de Spring Security
@Getter @Setter
public class UserMain implements UserDetails {

    private String firstName;
    private String lastName;
    private String email;
    private String encryptedPassword;
    //Variable que nos da la autorización (no confundir con autenticación) Coleccion de tipo generico que extendiende de GranthedAuthority de Spring security
    private Collection<? extends GrantedAuthority> authorities;

    public UserMain(final String firstName, final String lastName, final String email, final String encryptedPassword, final Collection<? extends GrantedAuthority> authorities) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.encryptedPassword = encryptedPassword;
        this.authorities = authorities;
    }

    //Metodo que asigna los privilegios (autorización)
    public static UserMain build(final UserEntity userEntity) {
        //Convertimos la clase Rol a la clase GrantedAuthority
        final List<GrantedAuthority> authorities =
                userEntity.getRoles()
                        .stream()
                        .map(rol -> new SimpleGrantedAuthority(rol.getName().name()))
                        .collect(Collectors.toUnmodifiableList());
        return new UserMain(userEntity.getFirstName(), userEntity.getLastName(),userEntity.getEmail(), userEntity.getEncryptedPassword(), authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return encryptedPassword;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
