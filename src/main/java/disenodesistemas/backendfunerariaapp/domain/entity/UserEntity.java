package disenodesistemas.backendfunerariaapp.domain.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "users")
@Table(indexes = {@Index(columnList = "email", name = "index_email", unique = true)})
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@Setter
public class UserEntity implements Serializable {

  @Id @GeneratedValue private Long id;

  @Column(nullable = false, length = 90)
  private String firstName;

  @Column(nullable = false, length = 90)
  private String lastName;

  @Column(nullable = false, length = 150)
  private String email;

  @Column(nullable = false)
  private String encryptedPassword;

  @CreatedDate private LocalDate startDate;

  @Column private boolean enabled;

  @Column(nullable = false)
  private Boolean active;

  // Roles are LAZY because EAGER on @ManyToMany silently triggers extra joins and N+1 risks
  // on every user lookup. Read paths that need roles (login, loadUserByUsername, role mutation)
  // are already executed inside @Transactional, and UserRepository.findByEmail uses an
  // @EntityGraph to fetch roles in a single query so the auth flow stays N+1-free.
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "user_role",
      joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
  private Set<RoleEntity> roles;

  // Aggregate-owned children: cascade ALL is appropriate because removing the user must
  // also drop their personal contact data and pending tokens.
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "userNumber", orphanRemoval = true)
  private List<MobileNumberEntity> mobileNumbers;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "userAddress", orphanRemoval = true)
  private List<AddressEntity> addresses;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
  private List<AffiliateEntity> affiliates;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
  private List<ConfirmationTokenEntity> confirmationTokens;

  // Transactional history (deceased registrations and recorded incomes) is NOT cascaded on
  // remove and orphanRemoval is disabled. These records must outlive the user that registered
  // them for accounting and audit reasons; deleting a user must never sweep away the
  // funeral home's financial or operational history. Persistence and merge are still
  // cascaded so adding an income via the user aggregate keeps working.
  @OneToMany(
      cascade = {CascadeType.PERSIST, CascadeType.MERGE},
      mappedBy = "deceasedUser")
  private List<DeceasedEntity> deceasedList;

  @OneToMany(
      cascade = {CascadeType.PERSIST, CascadeType.MERGE},
      mappedBy = "incomeUser")
  private List<IncomeEntity> incomes;

  public UserEntity(
      final String email,
      final String firstName,
      final String lastName,
      final String encryptedPassword) {
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

  public void activate() {
    this.active = true;
  }

  public void deactivate() {
    this.active = false;
  }

  public void setMobileNumbers(final List<MobileNumberEntity> mobileNumbers) {
    if (this.mobileNumbers == null) {
      this.mobileNumbers = new ArrayList<>();
    } else {
      this.mobileNumbers.clear();
    }
    mobileNumbers.forEach(this::addMobileNumber);
  }

  public void addMobileNumber(final MobileNumberEntity mobileNumber) {
    if (!this.mobileNumbers.contains(mobileNumber)) {
      mobileNumbers.add(mobileNumber);
      mobileNumber.setUserNumber(this);
    }
  }

  public void removeMobileNumber(final MobileNumberEntity mobileNumber) {
    mobileNumbers.remove(mobileNumber);
    mobileNumber.setUserNumber(null);
  }

  public void setAddresses(final List<AddressEntity> addresses) {
    if (this.addresses == null) {
      this.addresses = new ArrayList<>();
    } else {
      this.addresses.clear();
    }
    addresses.forEach(this::addAddress);
  }

  public void addAddress(final AddressEntity address) {
    if (!this.addresses.contains(address)) {
      this.addresses.add(address);
      address.setUserAddress(this);
    }
  }

  public void removeAddress(final AddressEntity address) {
    this.addresses.remove(address);
    address.setUserAddress(null);
  }

  public void setRol(final List<RoleEntity> roleEntities) {
    if (this.roles == null) {
      this.roles = new java.util.HashSet<>();
    } else {
      this.roles.clear();
    }
    roleEntities.forEach(this::addRol);
  }

  public void addRol(final RoleEntity roleEntity) {
    if (!this.roles.contains(roleEntity)) {
      roles.add(roleEntity);
    }
  }

  public void removeRol(final RoleEntity roleEntity) {
    this.roles.remove(roleEntity);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    final UserEntity that = (UserEntity) o;
    return id != null && Objects.equals(email, that.getEmail());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
