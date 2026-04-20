package disenodesistemas.backendfunerariaapp.application.usecase.funeral;

import disenodesistemas.backendfunerariaapp.application.port.out.FuneralPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.FuneralMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.FuneralRequestDto;
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

  private final FuneralPersistencePort funeralPersistencePort;
  private final FuneralMapper funeralMapper;
  private final PlanQueryUseCase planQueryUseCase;
  private final FuneralDeceasedUseCase funeralDeceasedUseCase;
  private final FuneralDraftFactory funeralDraftFactory;
  private final FuneralQueryUseCase funeralQueryUseCase;

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
    return updatedFuneral;
  }

  @Transactional
  public void delete(final Long id) {
    funeralPersistencePort.delete(funeralQueryUseCase.findEntityById(id));
    logFuneralDeleted(id);
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
