package disenodesistemas.backendfunerariaapp.application.usecase.plan;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxPort;
import disenodesistemas.backendfunerariaapp.application.port.out.PlanPersistencePort;
import disenodesistemas.backendfunerariaapp.application.support.PlanItemService;
import disenodesistemas.backendfunerariaapp.application.support.PlanPricingService;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.domain.event.PlanDeleted;
import disenodesistemas.backendfunerariaapp.mapping.PlanMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.PlanResponseDto;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class PlanCommandUseCase {

  private static final String AUDIT_TARGET_TYPE = "PLAN";

  private final PlanPersistencePort planPersistencePort;
  private final PlanMapper planMapper;
  private final PlanItemService planItemService;
  private final PlanPricingService planPricingService;
  private final PlanQueryUseCase planQueryUseCase;
  private final AuthenticatedUserPort authenticatedUserPort;
  private final AuditEventPort auditEventPort;
  private final OutboxPort outboxPort;
  /**
   * Wall-clock read used for soft-delete tombstones. Wired from the shared
   * {@code TimeConfig} bean ({@link Clock#systemUTC()} in production, fixed at a
   * known instant in tests). Centralizing the clock in one bean avoids the
   * dual-constructor boilerplate the older use cases carry.
   */
  private final Clock clock;

  @Transactional
  public PlanResponseDto create(final PlanRequestDto planRequestDto) {
    final Plan planEntity = planMapper.toEntity(planRequestDto);
    planEntity.setItemsPlan(Set.of());
    planEntity.setFuneral(new ArrayList<>());
    final Plan savedPlan = planPersistencePort.save(planEntity);
    final Set<ItemPlanEntity> itemPlanEntities =
        planItemService.buildItemsPlan(planRequestDto.itemsPlan(), savedPlan);
    savedPlan.setItemsPlan(itemPlanEntities);
    savedPlan.setPrice(
        planPricingService.calculatePrice(savedPlan.getProfitPercentage(), itemPlanEntities));
    final PlanResponseDto created = planMapper.toDto(planPersistencePort.save(savedPlan));
    recordPlanCreated(created);
    return created;
  }

  @Transactional
  public PlanResponseDto update(final Long id, final PlanRequestDto planRequestDto) {
    final Plan planToUpdate = planQueryUseCase.findPlanById(id);
    planMapper.updateEntity(planRequestDto, planToUpdate);
    if (!CollectionUtils.isEmpty(planRequestDto.itemsPlan())) {
      final List<ItemPlanEntity> itemPlanEntitiesDeleted =
          planItemService.getDeletedItemsPlanEntities(planToUpdate, planRequestDto.itemsPlan());
      itemPlanEntitiesDeleted.forEach(planToUpdate::removeItemToPlan);
      planToUpdate.setItemsPlan(
          planItemService.buildItemsPlan(planRequestDto.itemsPlan(), planToUpdate));
    }
    planToUpdate.setPrice(
        planPricingService.calculatePrice(
            planToUpdate.getProfitPercentage(), planToUpdate.getItemsPlan()));
    return planMapper.toDto(planPersistencePort.save(planToUpdate));
  }

  /**
   * Soft-deletes the plan identified by {@code id}: stamps {@code deletedAt = now()}
   * and {@code deletedBy = <actor email>}, then saves. The row stays in the DB so the
   * admin-only papelera surface can still surface it, but every regular read filters
   * it out (see {@link disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.PlanRepository}).
   *
   * <p>An {@link AuditAction#PLAN_DELETED} entry is still recorded so the audit log
   * carries the operator-readable trail; the id is captured before the save so it
   * survives even if a future change ever drops the row physically.
   */
  @Transactional
  public void delete(final Long id) {
    final Plan plan = planQueryUseCase.findPlanById(id);
    final UserEntity actor = authenticatedUserPort.getAuthenticatedUser();

    plan.setDeletedAt(Instant.now(clock));
    plan.setDeletedBy(actor.getEmail());
    planPersistencePort.save(plan);

    auditEventPort.record(
        AuditAction.PLAN_DELETED,
        actor.getEmail(),
        actor.getId(),
        AUDIT_TARGET_TYPE,
        String.valueOf(id),
        null);

    outboxPort.publish(new PlanDeleted(id));
  }

  /**
   * Emits the audit entry for a successful plan creation. Payload carries the plan
   * name + the count of items in the plan so audit consumers get a meaningful
   * one-liner without joining back to the plan table.
   */
  private void recordPlanCreated(final PlanResponseDto created) {
    final UserEntity actor = authenticatedUserPort.getAuthenticatedUser();
    final int itemsCount = created.itemsPlan() == null ? 0 : created.itemsPlan().size();
    final String payload =
        "{\"name\":\"" + escape(created.name()) + "\",\"itemsCount\":" + itemsCount + "}";
    auditEventPort.record(
        AuditAction.PLAN_CREATED,
        actor.getEmail(),
        actor.getId(),
        AUDIT_TARGET_TYPE,
        String.valueOf(created.id()),
        payload);
  }

  /**
   * Minimal JSON-string escape so plan names with embedded quotes / backslashes do
   * not break the audit payload. The audit log stores raw JSON text — a malformed
   * object here would only surface much later in a downstream consumer.
   */
  private static String escape(final String raw) {
    if (raw == null) {
      return "";
    }
    return raw.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
