package disenodesistemas.backendfunerariaapp.domain.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "affiliates")
@Table(indexes = { @Index(columnList = "dni", name = "index_dni", unique = true) })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class AffiliateEntity implements Serializable {
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 70)
    private String lastName;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false)
    private Integer dni;

    @Column(nullable = false)
    private LocalDate birthDate;

    /** Audit stamp: the day the record was created. Not a business date; see {@link #enrolmentDate}. */
    @CreatedDate
    private LocalDate startDate;

    /**
     * Enrolment date ("fecha de alta"): the day the affiliate joined the membership, which drives
     * the claim waiting period (ADR-0022) and the first billable month. Set explicitly on create
     * and editable on update, so back-dated and legacy affiliates carry their real date.
     */
    @Column(name = "enrolment_date", nullable = false)
    private LocalDate enrolmentDate;

    @Column(name = "deceased", columnDefinition = "boolean default false")
    private Boolean deceased;

    /**
     * Soft-delete tombstone. Populated by {@code AffiliateCommandUseCase.delete} with the
     * UTC moment the record was removed; {@code null} for active affiliates. Every operational
     * read filters on {@code deletedAt is null} so soft-deleted rows are invisible to the
     * normal listings. The admin-only papelera endpoint queries the inverse.
     */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * Email of the admin that requested the soft delete. Captured from
     * {@code AuthenticatedUserPort} at delete time so the papelera can show "borrado por"
     * without joining the audit log. {@code null} for active affiliates.
     */
    @Column(name = "deleted_by", length = 255)
    private String deletedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_id")
    private GenderEntity gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_id")
    private RelationshipEntity relationship;

    /** Who enrols, pays and claims for this affiliate. Null only for pre-V18 legacy rows. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holder_id")
    private MembershipHolderEntity holder;

    /** Health tier the fee was priced with (also drives the claim waiting period). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_tier_id")
    private HealthTierEntity healthTier;

    /** Age band the fee was priced with; a different band at a later date means repricing. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "age_band_id")
    private AgeBandEntity ageBand;

    /** Monthly fee snapshot: base x age multiplier x health multiplier at {@link #feeCalculatedOn}. */
    @Column(name = "monthly_fee", precision = 12, scale = 2)
    private BigDecimal monthlyFee;

    @Column(name = "fee_calculated_on")
    private LocalDate feeCalculatedOn;

    /**
     * Callers that do not state an enrolment date (seeds, internal callers) keep the behaviour from
     * before the column existed: enrolled the day the record is created. Entity listeners run
     * before this callback (JPA 3.1, section 3.5.4), so {@link #startDate} is already stamped and
     * the JVM-zone {@code LocalDate.now()} branch only guards a disabled auditing listener. The
     * use case path never reaches it: it sets the date from the business-zone clock.
     */
    @PrePersist
    void defaultEnrolmentDate() {
        if (enrolmentDate == null) {
            enrolmentDate = startDate != null ? startDate : LocalDate.now();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final AffiliateEntity that = (AffiliateEntity) o;
        return id != null && Objects.equals(firstName, that.getFirstName()) &&
                Objects.equals(lastName, that.getLastName()) && Objects.equals(dni, that.getDni());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
