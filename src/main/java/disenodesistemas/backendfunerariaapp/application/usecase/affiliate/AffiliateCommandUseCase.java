package disenodesistemas.backendfunerariaapp.application.usecase.affiliate;

import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxPort;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.domain.event.AffiliateCreated;
import disenodesistemas.backendfunerariaapp.domain.event.AffiliateDeleted;
import disenodesistemas.backendfunerariaapp.domain.event.AffiliateMarkedDeceased;
import disenodesistemas.backendfunerariaapp.domain.event.AffiliateUpdated;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.AffiliateMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.AffiliateResponseDto;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AffiliateCommandUseCase {

  private static final String AUDIT_TARGET_TYPE = "AFFILIATE";

  private final AffiliatePersistencePort affiliatePersistencePort;
  private final AffiliateMapper affiliateMapper;
  private final AuthenticatedUserPort authenticatedUserPort;
  private final AffiliateQueryUseCase affiliateQueryUseCase;
  private final AuditEventPort auditEventPort;
  private final OutboxPort outboxPort;
  /**
   * Wall-clock read used for {@link AffiliateMarkedDeceased} and soft-delete
   * tombstones. Wired from the shared {@code TimeConfig} bean
   * ({@link Clock#systemUTC()} in production, fixed at a known instant in tests).
   */
  private final Clock clock;

  /**
   * Persists a new affiliate owned by the currently authenticated user, then records an
   * {@link AuditAction#AFFILIATE_CREATED} audit entry so compliance reviews can reconstruct
   * who registered each affiliate. The dni is used as the audit {@code targetId} because the
   * domain treats it as the affiliate's stable identifier.
   */
  @Transactional
  public AffiliateResponseDto create(final AffiliateRequestDto affiliate) {
    final AffiliateEntity affiliateEntity = affiliateMapper.toEntity(affiliate);
    final UserEntity actor = authenticatedUserPort.getAuthenticatedUser();
    affiliateEntity.setUser(actor);
    affiliateEntity.setDeceased(Boolean.FALSE);
    final AffiliateEntity saved = affiliatePersistencePort.save(affiliateEntity);

    auditEventPort.record(
        AuditAction.AFFILIATE_CREATED,
        actor.getEmail(),
        actor.getId(),
        AUDIT_TARGET_TYPE,
        String.valueOf(saved.getDni()),
        null);

    outboxPort.publish(
        new AffiliateCreated(
            saved.getDni(),
            saved.getFirstName(),
            saved.getLastName(),
            saved.getBirthDate(),
            saved.getGender() == null ? null : saved.getGender().getName(),
            saved.getRelationship() == null ? null : saved.getRelationship().getName(),
            actor.getEmail()));

    return affiliateMapper.toDto(saved);
  }

  /**
   * Updates the affiliate identified by the supplied dni, rejecting attempts to migrate it to
   * a dni that already belongs to another affiliate. Update events are intentionally not
   * audited at this stage; the audit catalog only enumerates {@code AFFILIATE_CREATED} and
   * {@code AFFILIATE_DELETED}, the events with the highest compliance value, and update
   * support can be added later by extending {@link AuditAction} together with the matching
   * {@code record} call here.
   */
  @Transactional
  public AffiliateResponseDto update(final Integer dni, final AffiliateRequestDto affiliate) {
    final AffiliateEntity affiliateToUpdate = affiliateQueryUseCase.findByDni(dni);

    if (Boolean.TRUE.equals(affiliatePersistencePort.existsByDni(affiliate.dni()))
        && !Objects.equals(affiliateToUpdate.getDni(), affiliate.dni())) {
      throw new ConflictException("affiliate.error.dni.already.exists");
    }

    // Snapshot the lifecycle flag before the mapper applies the request so we can detect
    // the false→true transition and emit AffiliateMarkedDeceased alongside the generic update.
    final boolean wasDeceased = Boolean.TRUE.equals(affiliateToUpdate.getDeceased());
    affiliateMapper.updateEntity(affiliate, affiliateToUpdate);
    final AffiliateEntity saved = affiliatePersistencePort.save(affiliateToUpdate);

    outboxPort.publish(
        new AffiliateUpdated(
            saved.getDni(),
            saved.getFirstName(),
            saved.getLastName(),
            saved.getBirthDate(),
            saved.getGender() == null ? null : saved.getGender().getName(),
            saved.getRelationship() == null ? null : saved.getRelationship().getName(),
            Boolean.TRUE.equals(saved.getDeceased())));

    if (!wasDeceased && Boolean.TRUE.equals(saved.getDeceased())) {
      outboxPort.publish(new AffiliateMarkedDeceased(saved.getDni(), Instant.now(clock)));
    }

    return affiliateMapper.toDto(saved);
  }

  /**
   * Soft-deletes the affiliate identified by the supplied dni: stamps
   * {@code deletedAt = now()} and {@code deletedBy = <actor email>}, then saves. The row
   * stays in the DB so the admin-only papelera surface can still surface it, but every
   * regular read filters it out (see {@code AffiliateRepository}). The dni stays globally
   * unique — a future create with the same dni keeps hitting the existing 409 path.
   *
   * <p>An {@link AuditAction#AFFILIATE_DELETED} entry is still recorded so the audit log
   * carries the operator-readable trail; the dni is captured before the save so it survives
   * even if a future change ever drops the row physically.
   */
  @Transactional
  public void delete(final Integer dni) {
    final AffiliateEntity affiliate = affiliateQueryUseCase.findByDni(dni);
    final UserEntity actor = authenticatedUserPort.getAuthenticatedUser();

    affiliate.setDeletedAt(Instant.now(clock));
    affiliate.setDeletedBy(actor.getEmail());
    affiliatePersistencePort.save(affiliate);

    auditEventPort.record(
        AuditAction.AFFILIATE_DELETED,
        actor.getEmail(),
        actor.getId(),
        AUDIT_TARGET_TYPE,
        String.valueOf(dni),
        null);

    outboxPort.publish(new AffiliateDeleted(dni));
  }
}
