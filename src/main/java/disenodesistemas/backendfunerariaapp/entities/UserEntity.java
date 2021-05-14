package disenodesistemas.backendfunerariaapp.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import disenodesistemas.backendfunerariaapp.appuser.AppUserRole;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

@Entity(name = "users")
@Table(indexes = { @Index(columnList = "userId", name = "index_userid", unique = true), @Index(columnList = "email", name = "index_email", unique = true) })
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@EqualsAndHashCode
@NoArgsConstructor
public class UserEntity implements UserDetails {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, length = 75)
    private String firstName;

    @Column(nullable = false, length = 75)
    private String lastName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false)
    private String encryptedPassword;

    @CreatedDate
    private Date startDate;

    @Enumerated(EnumType.STRING)
    private AppUserRole appUserRole;

    private Boolean locked = false;

    private Boolean enabled = false;


    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userNumber")
    private List<MobileNumberEntity> mobileNumbers = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userAddress")
    private List<AddressEntity> addresses = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<AffiliateEntity> affiliates = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "deceasedUser")
    private List<DeceasedEntity> deceasedList = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entryUser")
    @JsonManagedReference
    private List<EntryEntity> entries = new ArrayList<>();


    public UserEntity(String firstName, String lastName, String email, String encryptedPassword,AppUserRole appUserRole) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.encryptedPassword = encryptedPassword;
        this.appUserRole = appUserRole;

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(appUserRole.name());
        return Collections.singletonList(authority);
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
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
