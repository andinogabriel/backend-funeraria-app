package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "affiliates")
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
public class AffiliateEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String affiliateId;

    @Column(nullable = false, length = 70)
    private String lastName;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false)
    private Integer dni;

    @Column(nullable = false)
    private Date birthDate;

    @CreatedDate
    private Date startDate;

    @ManyToOne
    @JoinColumn(name = "gender_id")
    private GenderEntity affiliateGender;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity affiliateUser;

    @ManyToOne
    @JoinColumn(name = "kinship_id")
    private RelationshipEntity affiliateRelationship;


}
