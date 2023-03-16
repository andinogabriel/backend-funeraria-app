package disenodesistemas.backendfunerariaapp.entities;

import lombok.Builder;
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
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "deceased")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class DeceasedEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 150)
    private String firstName;

    @Column(nullable = false)
    private Integer dni;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private LocalDateTime deathDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private AddressEntity placeOfDeath;

    @CreatedDate
    private LocalDateTime registerDate;

    private boolean affiliated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_id")
    private RelationshipEntity deceasedRelationship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity deceasedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_id")
    private GenderEntity gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "death_cause_id")
    private DeathCauseEntity deathCause;

    //@PrimaryKeyJoinColumn
    @OneToOne(mappedBy = "deceased")
    private Funeral funeral;

    @Builder
    public DeceasedEntity(final String lastName, final String firstName, final Integer dni, final LocalDate birthDate,
                          final LocalDateTime deathDate, final AddressEntity placeOfDeath,
                          final RelationshipEntity deceasedRelationship, final boolean affiliated,
                          final UserEntity deceasedUser, final GenderEntity gender, final DeathCauseEntity deathCause) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.dni = dni;
        this.birthDate = birthDate;
        this.deathDate = deathDate;
        this.placeOfDeath = placeOfDeath;
        this.deceasedRelationship = deceasedRelationship;
        this.affiliated = affiliated;
        this.deceasedUser = deceasedUser;
        this.gender = gender;
        this.deathCause = deathCause;
        this.registerDate = LocalDateTime.now();
    }
}
