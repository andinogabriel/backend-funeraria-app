package disenodesistemas.backendfunerariaapp.domain.entity;

import java.io.Serializable;
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

    @CreatedDate
    private LocalDate startDate;

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
