package disenodesistemas.backendfunerariaapp.application.usecase.funeral;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.FuneralPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxPort;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.domain.event.FuneralCreated;
import disenodesistemas.backendfunerariaapp.domain.event.FuneralDeleted;
import disenodesistemas.backendfunerariaapp.domain.event.FuneralUpdated;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.FuneralMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.FuneralRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeceasedResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.FuneralResponseDto;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FuneralCommandUseCase {

  private static final String AUDIT_TARGET_TYPE = "FUNERAL";

  private final FuneralPersistencePort funeralPersistencePort;
  private final FuneralMapper funeralMapper;
  private final PlanQueryUseCase planQueryUseCase;
  private final FuneralDeceasedUseCase funeralDeceasedUseCase;
  private final FuneralDraftFactory funeralDraftFactory;
  private final FuneralQueryUseCase funeralQueryUseCase;
  private final AuthenticatedUserPort authenticatedUserPort;
  private final AuditEventPort auditEventPort;
  private final OutboxPort outboxPort;

  @Transactional
  public FuneralResponseDto create(final FuneralRequestDto funeralRequest) {
    logFuneralStarted("funeral.create.started", null, funeralRequest);
    validateReceiptNumber(funeralRequest.receiptNumber());

    final Plan funeralPlan = planQueryUseCase.findEntityById(funeralRequest.plan().id());
    final Funeral funeral =
        funeralDraftFactory.create(
            funeralRequest,
            funeralPlan,
            funeralDeceasedUseCase.registerDeceased(funeralRequest.deceased()));

    final FuneralResponseDto createdFuneral = funeralMapper.toDto(funeralPersistencePort.save(funeral));
    logFuneralCompleted("funeral.create.completed", createdFuneral);
    recordFuneralCreated(createdFuneral);
    outboxPort.publish(toFuneralCreated(createdFuneral));
    return createdFuneral;
  }

  @Transactional
  public FuneralResponseDto update(final Long id, final FuneralRequestDto funeralRequest) {
    logFuneralStarted("funeral.update.started", id, funeralRequest);
    final Funeral funeralToUpdate = funeralQueryUseCase.findEntityById(id);
    validateUniqueReceiptNumber(funeralRequest.receiptNumber(), funeralToUpdate.getReceiptNumber());

    final Plan funeralPlan = planQueryUseCase.findEntityById(funeralRequest.plan().id());
    funeralDraftFactory.update(funeralToUpdate, funeralRequest, funeralPlan);
    final FuneralResponseDto updatedFuneral = funeralMapper.toDto(funeralPersistencePort.save(funeralToUpdate));
    logFuneralCompleted("funeral.update.completed", updatedFuneral);
    outboxPort.publish(toFuneralUpdated(updatedFuneral));
    return updatedFuneral;
  }

  @Transactional
  public void delete(final Long id) {
    funeralPersistencePort.delete(funeralQueryUseCase.findEntityById(id));
    logFuneralDeleted(id);
    recordFuneralDeleted(id);
    outboxPort.publish(new FuneralDeleted(id));
  }

  /**
   * Builds a {@link FuneralCreated} event from the persisted response DTO. Captures receipt
   * metadata + the deceased identity so downstream consumers can summarise the service
   * without joining back to the funeral table. The nested aggregates are nullable on the wire
   * but never on a successful create — defensive null-checks keep the helper robust against a
   * future shape change in the response DTO.
   */
  private FuneralCreated toFuneralCreated(final FuneralResponseDto created) {
    final DeceasedResponseDto deceased = created.deceased();
    return new FuneralCreated(
        created.id(),
        created.receiptNumber(),
        created.receiptSeries(),
        created.totalAmount(),
        created.plan() == null ? null : created.plan().id(),
        deceased == null ? null : deceased.dni(),
        deceased == null ? null : deceased.firstName() + " " + deceased.lastName());
  }

  /** Same shape as {@link #toFuneralCreated(FuneralResponseDto)}; kept separate for clarity. */
  private FuneralUpdated toFuneralUpdated(final FuneralResponseDto updated) {
    final DeceasedResponseDto deceased = updated.deceased();
    return new FuneralUpdated(
        updated.id(),
        updated.receiptNumber(),
        updated.receiptSeries(),
        updated.totalAmount(),
        updated.plan() == null ? null : updated.plan().id(),
        deceased == null ? null : deceased.dni(),
        deceased == null ? null : deceased.firstName() + " " + deceased.lastName());
  }

  /**
   * Emits the audit entry for a successful funeral creation. Payload carries the receipt
   * number and plan id so audit consumers can reconstruct the contract a funeral was sold
   * under without joining back to the funeral table.
   */
  private void recordFuneralCreated(final FuneralResponseDto created) {
    final UserEntity actor = authenticatedUserPort.getAuthenticatedUser();
    final String payload =
        "{\"receiptNumber\":"
            + (created.receiptNumber() == null ? "null" : "\"" + created.receiptNumber() + "\"")
            + ",\"planId\":"
            + (created.plan() == null ? "null" : created.plan().id())
            + "}";
    auditEventPort.record(
        AuditAction.FUNERAL_CREATED,
        actor.getEmail(),
        actor.getId(),
        AUDIT_TARGET_TYPE,
        String.valueOf(created.id()),
        payload);
  }

  /**
   * Emits the audit entry for a successful funeral deletion. The funeral id is captured at
   * the call site because the entity is gone by the time the audit row is persisted.
   */
  private void recordFuneralDeleted(final Long funeralId) {
    final UserEntity actor = authenticatedUserPort.getAuthenticatedUser();
    auditEventPort.record(
        AuditAction.FUNERAL_DELETED,
        actor.getEmail(),
        actor.getId(),
        AUDIT_TARGET_TYPE,
        String.valueOf(funeralId),
        null);
  }

  private void validateReceiptNumber(final String receiptNumber) {
    if (StringUtils.isNotBlank(receiptNumber)
        && funeralPersistencePort.existsByReceiptNumber(receiptNumber)) {
      logReceiptNumberRejected(receiptNumber);
      throw new ConflictException("funeral.error.receiptNumber.already.exists");
    }
  }

  private void validateUniqueReceiptNumber(
      final String newReceiptNumber, final String oldReceiptNumber) {
    if (StringUtils.isBlank(newReceiptNumber)) {
      return;
    }

    if (funeralPersistencePort.existsByReceiptNumber(newReceiptNumber)
        && !Objects.equals(newReceiptNumber, oldReceiptNumber)) {
      logReceiptNumberRejected(newReceiptNumber);
      throw new ConflictException("funeral.error.receiptNumber.already.exists");
    }
  }

  private void logFuneralStarted(
      final String event, final Long funeralId, final FuneralRequestDto funeralRequest) {
    final var builder =
        log.atInfo()
            .addKeyValue("event", event)
            .addKeyValue("planId", funeralRequest.plan().id())
            .addKeyValue("receiptNumberProvided", StringUtils.isNotBlank(funeralRequest.receiptNumber()));

    if (funeralId != null) {
      builder.addKeyValue("funeralId", funeralId);
    }
    builder.log(event);
  }

  private void logFuneralCompleted(final String event, final FuneralResponseDto funeralResponse) {
    log.atInfo()
        .addKeyValue("event", event)
        .addKeyValue("funeralId", funeralResponse.id())
        .addKeyValue("receiptNumber", funeralResponse.receiptNumber())
        .log(event);
  }

  private void logReceiptNumberRejected(final String receiptNumber) {
    log.atWarn()
        .addKeyValue("event", "funeral.receipt_number.rejected")
        .addKeyValue("receiptNumber", receiptNumber)
        .addKeyValue("reason", "already_exists")
        .log("funeral.receipt_number.rejected");
  }

  private void logFuneralDeleted(final Long funeralId) {
    log.atInfo()
        .addKeyValue("event", "funeral.delete.completed")
        .addKeyValue("funeralId", funeralId)
        .log("funeral.delete.completed");
  }
}
