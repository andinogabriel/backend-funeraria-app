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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AffiliateCommandUseCase {

  private static final String AUDIT_TARGET_TYPE = "AFFILIATE";

  private final AffiliatePersistencePort affiliatePersistencePort;
  private final AffiliateMapper affiliateMapper;
  private final AuthenticatedUserPort authenticatedUserPort;
  private final AffiliateQueryUseCase affiliateQueryUseCase;
  private final AuditEventPort auditEventPort;
  private final OutboxPort outboxPort;
  private final Clock clock;

  /** Production-time constructor wired by Spring; defaults the clock to {@link Clock#systemUTC()}. */
  @Autowired
  public AffiliateCommandUseCase(
      final AffiliatePersistencePort affiliatePersistencePort,
      final AffiliateMapper affiliateMapper,
      final AuthenticatedUserPort authenticatedUserPort,
      final AffiliateQueryUseCase affiliateQueryUseCase,
      final AuditEventPort auditEventPort,
      final OutboxPort outboxPort) {
    this(
        affiliatePersistencePort,
        affiliateMapper,
        authenticatedUserPort,
        affiliateQueryUseCase,
        auditEventPort,
        outboxPort,
        Clock.systemUTC());
  }

  /** Test-friendly overload that lets a deterministic clock drive {@link AffiliateMarkedDeceased}. */
  public AffiliateCommandUseCase(
      final AffiliatePersistencePort affiliatePersistencePort,
      final AffiliateMapper affiliateMapper,
      final AuthenticatedUserPort authenticatedUserPort,
      final AffiliateQueryUseCase affiliateQueryUseCase,
      final AuditEventPort auditEventPort,
      final OutboxPort outboxPort,
      final Clock clock) {
    this.affiliatePersistencePort = affiliatePersistencePort;
    this.affiliateMapper = affiliateMapper;
    this.authenticatedUserPort = authenticatedUserPort;
    this.affiliateQueryUseCase = affiliateQueryUseCase;
    this.auditEventPort = auditEventPort;
    this.outboxPort = outboxPort;
    this.clock = clock;
  }

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
   * Deletes the affiliate identified by the supplied dni and records an
   * {@link AuditAction#AFFILIATE_DELETED} entry stamped with the requesting admin. The dni
   * is captured before the delete so it survives in the audit row even though the underlying
   * entity is gone.
   */
  @Transactional
  public void delete(final Integer dni) {
    final AffiliateEntity affiliate = affiliateQueryUseCase.findByDni(dni);
    affiliatePersistencePort.delete(affiliate);

    final UserEntity actor = authenticatedUserPort.getAuthenticatedUser();
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
