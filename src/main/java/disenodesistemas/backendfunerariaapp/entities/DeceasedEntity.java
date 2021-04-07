package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "deceased")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public class DeceasedEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String deceasedId;

    @Column(nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false, length = 150)
    private String firstName;

    @Column(nullable = false)
    private Integer dni;

    @Column(nullable = false)
    private Date birthDate;

    @Column(nullable = false)
    private Date deathDate;

    private AddressEntity placeOfDeath;

    @CreatedDate
    private Date registerDate;

    @ManyToOne
    @JoinColumn(name = "relationship_id")
    private RelationshipEntity deceasedRelationship;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity deceasedUser;

    @ManyToOne
    @JoinColumn(name = "gender_id")
    private GenderEntity deceasedGender;

    @ManyToOne
    @JoinColumn(name = "death_cause_id")
    private DeathCauseEntity deceasedDeathCause;

    @OneToOne(mappedBy = "deceased")
    private ServiceEntity service;


}
