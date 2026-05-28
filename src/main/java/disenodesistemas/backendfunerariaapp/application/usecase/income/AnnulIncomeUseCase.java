package disenodesistemas.backendfunerariaapp.application.usecase.income;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.IncomePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxPort;
import disenodesistemas.backendfunerariaapp.application.port.out.ReceiptNumberGeneratorPort;
import disenodesistemas.backendfunerariaapp.application.support.IncomeDetailService;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeDetailEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.domain.enums.IncomeStatus;
import disenodesistemas.backendfunerariaapp.domain.event.IncomeAnnulled;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.IncomeMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.IncomeResponseDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Annuls an existing income (compra). Replaces the legacy {@code delete} flow with a
 * contabilidad-grade cancellation that keeps the original receipt visible and creates a
 * matching reversal counter-entry, so the audit log can always reconstruct the cancellation
 * from the income table alone — no implicit "the row used to exist" inference needed.
 *
 * <h3>Three guards, all returning 409</h3>
 *
 * <ul>
 *   <li>The original is already {@code ANNULLED} ({@code income.error.annul.already.annulled}).
 *       Idempotency is intentionally <em>strict</em> — a second annul on the same id is a
 *       client bug, not a no-op. The strict 409 keeps the contract honest.</li>
 *   <li>The target is itself a reversal counter-entry
 *       ({@code income.error.annul.is.reversal}). Reversal rows are immutable by accounting
 *       contract; cancelling a reversal would require a re-reversal, which we deliberately do
 *       not support.</li>
 *   <li>At least one detail's item has less stock on hand than the original receipt's
 *       quantity ({@code income.error.annul.insufficient.stock}). The decision is to refuse
 *       the annul (instead of going negative) because anulling a compra <em>undoes</em> a
 *       past stock increment — letting it go negative would imply someone consumed stock the
 *       compra never actually delivered, which is precisely the inconsistency the annul flow
 *       exists to surface.</li>
 * </ul>
 *
 * <h3>Atomic effect</h3>
 *
 * Wrapped in a single {@code @Transactional}:
 *
 * <ol>
 *   <li>For each original detail, decrement the item's stock by {@code detail.quantity}.
 *       Mirrors {@code IncomeDetailService.restoreStock} verbatim — that helper was
 *       designed for the income {@code update} flow's "rollback before re-apply" pattern.</li>
 *   <li>Build a reversal {@link IncomeEntity} with negative quantities (the per-line subtotal
 *       and the total amount come out negative) and a fresh receipt number / series so the
 *       accounting trail has two distinct legal documents. Stamp
 *       {@code reversalOf = original} so consumers can join the pair.</li>
 *   <li>Mark the original as {@code ANNULLED}.</li>
 *   <li>Save reversal + original. The audit + outbox emissions ride on the same transaction.</li>
 * </ol>
 *
 * <h3>Prices stay untouched</h3>
 *
 * The decision (option A from the design discussion) is to leave each item's {@code price}
 * exactly where the most recent receipt put it. Restoring "the price before the cancelled
 * receipt" would require a historical price ledger that does not exist, and chasing it would
 * add complexity for almost no operator value — the next compra that touches the item
 * refreshes the price anyway.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnnulIncomeUseCase {

  private static final String AUDIT_TARGET_TYPE = "INCOME";

  private final IncomePersistencePort incomePersistencePort;
  private final IncomeQueryUseCase incomeQueryUseCase;
  private final IncomeDetailService incomeDetailService;
  private final IncomeMapper incomeMapper;
  private final ReceiptNumberGeneratorPort receiptNumberGeneratorPort;
  private final AuthenticatedUserPort authenticatedUserPort;
  private final AuditEventPort auditEventPort;
  private final OutboxPort outboxPort;

  /**
   * Annuls the income identified by {@code id} and returns the persisted reversal as a
   * response DTO. Callers (the controller) ship the reversal back to the operator so the UI
   * can immediately render the counter-entry alongside the (now {@code ANNULLED}) original.
   */
  @Transactional
  public IncomeResponseDto annul(final Long id) {
    final IncomeEntity original = incomeQueryUseCase.findEntityById(id);
    logAnnulStarted(id);
    validateNotAlreadyAnnulled(id, original);
    validateNotAReversal(id, original);
    validateStockSufficient(original);

    // Decrement each item's stock by the original receipt's quantity. We reuse
    // restoreStock verbatim — the helper was already designed for "undo the past
    // increment" in the income update flow.
    incomeDetailService.restoreStock(original.getIncomeDetails());

    final IncomeEntity reversal = buildReversal(original);
    final IncomeEntity savedReversal = incomePersistencePort.save(reversal);

    original.setStatus(IncomeStatus.ANNULLED);
    incomePersistencePort.save(original);

    recordAuditEvent(original.getId(), savedReversal.getId());
    outboxPort.publish(new IncomeAnnulled(original.getId(), savedReversal.getId()));
    logAnnulCompleted(id, savedReversal.getId());

    return incomeMapper.toDto(savedReversal);
  }

  /* -------------------------------- guards --------------------------------- */

  private void validateNotAlreadyAnnulled(final Long id, final IncomeEntity income) {
    if (income.getStatus() == IncomeStatus.ANNULLED) {
      logAnnulRejected(id, "already_annulled");
      throw new ConflictException("income.error.annul.already.annulled");
    }
  }

  private void validateNotAReversal(final Long id, final IncomeEntity income) {
    if (income.getReversalOf() != null) {
      logAnnulRejected(id, "is_reversal");
      throw new ConflictException("income.error.annul.is.reversal");
    }
  }

  /**
   * Refuses the annul when any detail's referenced item has less stock on hand than the
   * receipt's quantity. The decision is documented at the class level: undoing a compra
   * undoes a past stock increment; going negative would imply phantom consumption.
   */
  private void validateStockSufficient(final IncomeEntity income) {
    if (income.getIncomeDetails() == null) {
      return;
    }
    for (final IncomeDetailEntity detail : income.getIncomeDetails()) {
      if (detail == null || detail.getItem() == null) {
        continue;
      }
      final ItemEntity item = detail.getItem();
      final int currentStock = item.getStock() == null ? 0 : item.getStock();
      if (currentStock < detail.getQuantity()) {
        logAnnulRejected(income.getId(), "insufficient_stock");
        throw new ConflictException("income.error.annul.insufficient.stock");
      }
    }
  }

  /* ------------------------------- reversal -------------------------------- */

  /**
   * Builds a fresh {@link IncomeEntity} that mirrors the original with negative
   * quantities. Same supplier + receiptType + tax + items + per-line prices; the receipt
   * series + number get fresh sequential values so the accounting trail carries two
   * distinct legal documents.
   *
   * <p>Stock is NOT touched here — {@code restoreStock} already did the decrement in the
   * caller. The reversal record exists purely as a passive ledger entry.
   */
  private IncomeEntity buildReversal(final IncomeEntity original) {
    final UserEntity actor = authenticatedUserPort.getAuthenticatedUser();
    final IncomeEntity reversal =
        IncomeEntity.builder()
            .receiptNumber(receiptNumberGeneratorPort.nextReceiptNumber())
            .receiptSeries(receiptNumberGeneratorPort.nextSerialNumber())
            .tax(original.getTax())
            .receiptType(original.getReceiptType())
            .incomeSupplier(original.getSupplier())
            .incomeUser(actor)
            .build();
    reversal.setStatus(IncomeStatus.ACTIVE);
    reversal.setReversalOf(original);
    reversal.setIncomeDetails(buildReversalDetails(original));
    reversal.setTotalAmount(
        incomeDetailService.calculateTotal(reversal.getIncomeDetails(), reversal.getTax()));
    return reversal;
  }

  /**
   * Clones each original detail with a negated quantity. Item references + per-line prices
   * stay identical so the reversal can be read as a symmetric counter-entry — same products,
   * opposite quantity, same prices.
   */
  private List<IncomeDetailEntity> buildReversalDetails(final IncomeEntity original) {
    if (original.getIncomeDetails() == null || original.getIncomeDetails().isEmpty()) {
      return List.of();
    }
    final List<IncomeDetailEntity> reversalDetails =
        new ArrayList<>(original.getIncomeDetails().size());
    for (final IncomeDetailEntity src : original.getIncomeDetails()) {
      if (src == null) {
        continue;
      }
      reversalDetails.add(
          IncomeDetailEntity.builder()
              .item(src.getItem())
              .quantity(-src.getQuantity())
              .purchasePrice(src.getPurchasePrice())
              .salePrice(src.getSalePrice())
              .build());
    }
    return reversalDetails;
  }

  /* ------------------------------- audit + log ------------------------------ */

  private void recordAuditEvent(final Long originalId, final Long reversalId) {
    final UserEntity actor = authenticatedUserPort.getAuthenticatedUser();
    final String payload =
        "{\"originalId\":"
            + originalId
            + ",\"reversalId\":"
            + (reversalId == null ? "null" : reversalId)
            + "}";
    auditEventPort.record(
        AuditAction.INCOME_ANNULLED,
        actor.getEmail(),
        actor.getId(),
        AUDIT_TARGET_TYPE,
        String.valueOf(originalId),
        payload);
  }

  private void logAnnulStarted(final Long id) {
    log.atInfo()
        .addKeyValue("event", "income.annul.started")
        .addKeyValue("incomeId", id)
        .log("income.annul.started");
  }

  private void logAnnulCompleted(final Long originalId, final Long reversalId) {
    log.atInfo()
        .addKeyValue("event", "income.annul.completed")
        .addKeyValue("originalId", originalId)
        .addKeyValue("reversalId", reversalId)
        .log("income.annul.completed");
  }

  private void logAnnulRejected(final Long incomeId, final String reason) {
    log.atWarn()
        .addKeyValue("event", "income.annul.rejected")
        .addKeyValue("incomeId", Objects.toString(incomeId, "null"))
        .addKeyValue("reason", reason)
        .log("income.annul.rejected");
  }
}
