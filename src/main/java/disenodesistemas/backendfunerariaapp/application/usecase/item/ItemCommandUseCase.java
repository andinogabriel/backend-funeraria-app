package disenodesistemas.backendfunerariaapp.application.usecase.item;

import static java.util.Objects.nonNull;

import disenodesistemas.backendfunerariaapp.application.model.FilePayload;
import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.FileStoragePort;
import disenodesistemas.backendfunerariaapp.application.port.out.ItemPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxPort;
import disenodesistemas.backendfunerariaapp.application.port.out.PlanPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.domain.event.ItemDeleted;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.ItemMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ItemResponseDto;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemCommandUseCase {

  private static final String AUDIT_TARGET_TYPE = "ITEM";

  private final ItemPersistencePort itemPersistencePort;
  private final PlanPersistencePort planPersistencePort;
  private final ItemMapper itemMapper;
  private final FileStoragePort fileStoragePort;
  private final ItemQueryUseCase itemQueryUseCase;
  private final AuthenticatedUserPort authenticatedUserPort;
  private final AuditEventPort auditEventPort;
  private final OutboxPort outboxPort;
  /**
   * Wall-clock read used for soft-delete tombstones. Wired from the shared
   * {@code TimeConfig} bean ({@link Clock#systemUTC()} in production, fixed at
   * a known instant in tests).
   */
  private final Clock clock;

  /**
   * Mirrors the {@code items.low_stock_threshold NOT NULL DEFAULT 10} column. Used on the
   * create path so a request that omits the field still ends up with a sensible floor
   * rather than triggering an NPE-flavoured insert.
   */
  private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

  @Transactional
  public ItemResponseDto create(final ItemRequestDto itemRequestDto) {
    logItemCreateStarted(itemRequestDto);
    final ItemEntity itemEntity = itemMapper.toEntity(itemRequestDto);
    itemEntity.setCode(UUID.randomUUID().toString());
    if (itemEntity.getLowStockThreshold() == null) {
      itemEntity.setLowStockThreshold(DEFAULT_LOW_STOCK_THRESHOLD);
    }
    final ItemResponseDto createdItem = itemMapper.toDto(itemPersistencePort.save(itemEntity));
    recordItemCreated(createdItem);
    logItemCompleted("item.create.completed", createdItem);
    return createdItem;
  }

  @Transactional
  public ItemResponseDto update(final String code, final ItemRequestDto itemRequestDto) {
    logItemStarted("item.update.started", code);
    final ItemEntity itemEntity = itemQueryUseCase.getItemByCode(code);
    // Snapshot the threshold before the mapper applies the request so we can detect
    // genuine changes — MapStruct's IGNORE strategy leaves the field unchanged when
    // the DTO carries null, so we only audit when the operator explicitly sets a
    // different value.
    final Integer previousThreshold = itemEntity.getLowStockThreshold();
    itemMapper.updateEntity(itemRequestDto, itemEntity);
    final boolean thresholdChanged =
        itemRequestDto.lowStockThreshold() != null
            && !java.util.Objects.equals(previousThreshold, itemRequestDto.lowStockThreshold());
    final ItemResponseDto updatedItem = itemMapper.toDto(itemPersistencePort.save(itemEntity));
    if (thresholdChanged) {
      recordThresholdUpdated(itemEntity, previousThreshold, itemEntity.getLowStockThreshold());
    }
    logItemCompleted("item.update.completed", updatedItem);
    return updatedItem;
  }

  /**
   * Soft-deletes the item identified by {@code code}: stamps {@code deletedAt = now()}
   * and {@code deletedBy = <actor email>}, then saves. Two guards fire <em>before</em>
   * the write so the operator gets a clear {@code 409 Conflict} instead of a silent
   * orphan:
   *
   * <ul>
   *   <li>{@code stock > 0} — refusing the delete forces the operator to first regularise
   *       the inventory (anular ingreso, ajuste, etc.) so the audit trail of the item
   *       reaching zero stock stays explicit.</li>
   *   <li>At least one <em>active</em> plan references this item — the plan list filters
   *       out soft-deleted plans, so a deleted plan does not block the item delete.</li>
   * </ul>
   *
   * <p>The attached image file (if any) is intentionally <b>not</b> removed from storage:
   * the row stays alive in DB, the papelera UI may render the thumbnail, and a future
   * retention sweep can purge orphaned files when (if ever) the row gets hard-deleted.
   *
   * <p>An {@link AuditAction#ITEM_DELETED} entry is recorded so the audit log carries
   * the operator-readable trail, and an {@link ItemDeleted} outbox event ships for
   * downstream consumers (notifications, analytical sinks).
   */
  @Transactional
  public void delete(final String code) {
    final ItemEntity itemEntity = itemQueryUseCase.getItemByCode(code);
    logItemDeleteStarted(code, itemEntity);
    validateStockClearedForDelete(itemEntity);
    validateNoActivePlanReferences(itemEntity);

    final UserEntity actor = authenticatedUserPort.getAuthenticatedUser();
    itemEntity.setDeletedAt(Instant.now(clock));
    itemEntity.setDeletedBy(actor.getEmail());
    itemPersistencePort.save(itemEntity);

    auditEventPort.record(
        AuditAction.ITEM_DELETED,
        actor.getEmail(),
        actor.getId(),
        AUDIT_TARGET_TYPE,
        String.valueOf(itemEntity.getId()),
        "{\"code\":\"" + escape(itemEntity.getCode()) + "\"}");

    outboxPort.publish(new ItemDeleted(itemEntity.getId(), itemEntity.getCode()));
    logItemStarted("item.delete.completed", code);
  }

  @Transactional
  public void uploadItemImage(final String code, final FilePayload image) {
    final ItemEntity itemEntity = itemQueryUseCase.getItemByCode(code);
    Optional.ofNullable(image)
        .ifPresentOrElse(
            imageToUpload -> {
              logItemImageUploadStarted(code, imageToUpload);
              itemEntity.setItemImageLink(fileStoragePort.store(itemEntity, imageToUpload));
              itemPersistencePort.save(itemEntity);
              logItemStarted("item.image.upload.completed", code);
            },
            () -> logItemRejected(code, "item.image.upload.rejected", "null_payload"));
  }

  /**
   * Refuses the delete with {@code 409} when the item still has stock on hand. The
   * message keys live in {@code messages*.properties} alongside the other domain
   * conflict messages.
   */
  private void validateStockClearedForDelete(final ItemEntity item) {
    final Integer stock = item.getStock();
    if (stock != null && stock > 0) {
      log.atWarn()
          .addKeyValue("event", "item.delete.rejected")
          .addKeyValue("code", item.getCode())
          .addKeyValue("reason", "stock_not_cleared")
          .addKeyValue("stock", stock)
          .log("item.delete.rejected");
      throw new ConflictException("item.error.delete.stock.not.cleared");
    }
  }

  /**
   * Refuses the delete with {@code 409} when at least one active plan still references
   * the item. The plan persistence port's lookup already filters out soft-deleted plans,
   * so this guard only fires on genuinely live references — a plan that was deleted
   * earlier in the day does not block the item delete.
   */
  private void validateNoActivePlanReferences(final ItemEntity item) {
    final List<Plan> activePlans =
        planPersistencePort.findPlansContainingAnyOfThisItems(List.of(item));
    if (!activePlans.isEmpty()) {
      log.atWarn()
          .addKeyValue("event", "item.delete.rejected")
          .addKeyValue("code", item.getCode())
          .addKeyValue("reason", "active_plan_references")
          .addKeyValue("planCount", activePlans.size())
          .log("item.delete.rejected");
      throw new ConflictException("item.error.delete.plan.references");
    }
  }

  /**
   * Emits the audit entry for a successful item creation. Payload carries the item
   * name + code so audit consumers get a meaningful one-liner without joining back to
   * the items table.
   */
  private void recordItemCreated(final ItemResponseDto created) {
    final UserEntity actor = authenticatedUserPort.getAuthenticatedUser();
    final String payload =
        "{\"name\":\"" + escape(created.name()) + "\",\"code\":\"" + escape(created.code()) + "\"}";
    auditEventPort.record(
        AuditAction.ITEM_CREATED,
        actor.getEmail(),
        actor.getId(),
        AUDIT_TARGET_TYPE,
        created.code(),
        payload);
  }

  /**
   * Emits the audit entry for a low-stock threshold change. Payload carries the
   * previous and new values so audit consumers can trace how the floor evolved
   * over time without joining back to the items table.
   */
  private void recordThresholdUpdated(
      final ItemEntity item, final Integer previous, final Integer next) {
    final UserEntity actor = authenticatedUserPort.getAuthenticatedUser();
    final String payload =
        "{\"previous\":"
            + (previous == null ? "null" : previous.toString())
            + ",\"next\":"
            + (next == null ? "null" : next.toString())
            + ",\"code\":\""
            + escape(item.getCode())
            + "\"}";
    auditEventPort.record(
        AuditAction.ITEM_THRESHOLD_UPDATED,
        actor.getEmail(),
        actor.getId(),
        AUDIT_TARGET_TYPE,
        String.valueOf(item.getId()),
        payload);
  }

  /**
   * Minimal JSON-string escape so item names / codes with embedded quotes or
   * backslashes do not break the audit payload. Same helper pattern used by the
   * plan command use case.
   */
  private static String escape(final String raw) {
    if (raw == null) {
      return "";
    }
    return raw.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private void logItemCreateStarted(final ItemRequestDto itemRequestDto) {
    log.atInfo()
        .addKeyValue("event", "item.create.started")
        .addKeyValue("name", itemRequestDto.name())
        .log("item.create.started");
  }

  private void logItemStarted(final String event, final String code) {
    log.atInfo().addKeyValue("event", event).addKeyValue("code", code).log(event);
  }

  private void logItemCompleted(final String event, final ItemResponseDto itemResponseDto) {
    log.atInfo()
        .addKeyValue("event", event)
        .addKeyValue("code", itemResponseDto.code())
        .addKeyValue("name", itemResponseDto.name())
        .log(event);
  }

  private void logItemDeleteStarted(final String code, final ItemEntity itemEntity) {
    log.atInfo()
        .addKeyValue("event", "item.delete.started")
        .addKeyValue("code", code)
        .addKeyValue("hasImage", nonNull(itemEntity.getItemImageLink()))
        .log("item.delete.started");
  }

  private void logItemImageUploadStarted(final String code, final FilePayload imageToUpload) {
    log.atInfo()
        .addKeyValue("event", "item.image.upload.started")
        .addKeyValue("code", code)
        .addKeyValue("filename", imageToUpload.originalFilename())
        .addKeyValue("contentType", imageToUpload.contentType())
        .addKeyValue("size", imageToUpload.size())
        .log("item.image.upload.started");
  }

  private void logItemRejected(final String code, final String event, final String reason) {
    log.atWarn()
        .addKeyValue("event", event)
        .addKeyValue("code", code)
        .addKeyValue("reason", reason)
        .log(event);
  }
}
