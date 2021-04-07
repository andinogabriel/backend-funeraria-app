package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(name = "users")
@Table(indexes = { @Index(columnList = "userId", name = "index_userid", unique = true), @Index(columnList = "email", name = "index_email", unique = true) })
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public class UserEntity implements Serializable {

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

    @Column(nullable = false)
    private Integer dni;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false)
    private String encryptedPassword;

    @CreatedDate
    private Date startDate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userNumber")
    private List<MobileNumberEntity> mobileNumbers = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userAddress")
    private List<AddressEntity> addresses = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "affiliateUser")
    private List<AffiliateEntity> affiliates = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "deceasedUser")
    private List<DeceasedEntity> deceasedList = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entryUser")
    private List<EntryEntity> entries = new ArrayList<>();

}
