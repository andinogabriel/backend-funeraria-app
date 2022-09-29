package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity(name = "users")
@Table(indexes = { @Index(columnList = "email", name = "index_email", unique = true) })
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@Setter
public class UserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 90)
    private String firstName;

    @Column(nullable = false, length = 90)
    private String lastName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false)
    private String encryptedPassword;

    @CreatedDate
    private LocalDate startDate;

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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userNumber", orphanRemoval = true)
    private List<MobileNumberEntity> mobileNumbers;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userAddress", orphanRemoval = true)
    private List<AddressEntity> addresses;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
    private List<AffiliateEntity> affiliates;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "deceasedUser", orphanRemoval = true)
    private List<DeceasedEntity> deceasedList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "incomeUser", orphanRemoval = true)
    private List<IncomeEntity> incomes;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
    private List<ConfirmationTokenEntity> confirmationTokens;

    public UserEntity(final String email, final String firstName, final String lastName, final String encryptedPassword) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.encryptedPassword = encryptedPassword;
        this.enabled = false;
        this.mobileNumbers = new ArrayList<>();
        this.addresses = new ArrayList<>();
        this.affiliates = new ArrayList<>();
        this.deceasedList = new ArrayList<>();
        this.incomes = new ArrayList<>();
        this.confirmationTokens = new ArrayList<>();
    }


}
