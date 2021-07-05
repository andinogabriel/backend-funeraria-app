package disenodesistemas.backendfunerariaapp.entities;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity(name = "users")
@Table(indexes = { @Index(columnList = "email", name = "index_email", unique = true) })
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter @Setter
public class UserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false, length = 90)
    private String firstName;

    @Column(nullable = false, length = 90)
    private String lastName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false)
    private String encryptedPassword;

    @CreatedDate
    private Date startDate;

    @Column
    private boolean enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))
    private Set<RoleEntity> roles;


    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userNumber")
    private List<MobileNumberEntity> mobileNumbers = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userAddress")
    private List<AddressEntity> addresses = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<AffiliateEntity> affiliates = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "deceasedUser")
    private List<DeceasedEntity> deceasedList = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entryUser")
    private List<EntryEntity> entries = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<ConfirmationTokenEntity> confirmationTokens = new ArrayList<>();


    public UserEntity(String email, String firstName, String lastName, String encryptedPassword) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.encryptedPassword = encryptedPassword;
        this.enabled = false;
    }


}
