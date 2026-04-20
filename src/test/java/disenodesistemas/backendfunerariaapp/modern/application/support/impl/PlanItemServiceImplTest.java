package disenodesistemas.backendfunerariaapp.modern.application.support.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.ItemPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.PlanItemPersistencePort;
import disenodesistemas.backendfunerariaapp.application.support.impl.PlanItemServiceImpl;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.ItemPlanMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemPlanRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemRequestPlanDto;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("PlanItemServiceImpl")
class PlanItemServiceImplTest {

  @Mock private ItemPersistencePort itemPersistencePort;
  @Mock private PlanItemPersistencePort planItemPersistencePort;
  @Mock private ItemPlanMapper itemPlanMapper;

  @InjectMocks private PlanItemServiceImpl service;

  @Test
  @DisplayName(
      "Given request items with existing product codes when plan items are built then it maps, links and persists the aggregate collection")
  void givenRequestItemsWithExistingProductCodesWhenPlanItemsAreBuiltThenItMapsLinksAndPersistsTheAggregateCollection() {
    final Plan plan = new Plan("Plan Oro", "Cobertura completa", new BigDecimal("25.00"));
    plan.setId(1L);
    final ItemEntity urn = new ItemEntity();
    urn.setId(10L);
    urn.setCode("URN-001");
    final ItemPlanRequestDto request =
        ItemPlanRequestDto.builder()
            .item(ItemRequestPlanDto.builder().id(10L).name("Urna").code("URN-001").build())
            .quantity(2)
            .build();
    final ItemPlanEntity mapped = new ItemPlanEntity();

    when(itemPersistencePort.findAllByCodeIn(List.of("URN-001"))).thenReturn(List.of(urn));
    when(itemPlanMapper.toEntity(request)).thenReturn(mapped);
    when(planItemPersistencePort.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

    final Set<ItemPlanEntity> result = service.buildItemsPlan(Set.of(request), plan);

    assertThat(result).hasSize(1);
    final ItemPlanEntity itemPlan = result.iterator().next();
    assertThat(itemPlan.getPlan()).isEqualTo(plan);
    assertThat(itemPlan.getItem()).isEqualTo(urn);
    verify(planItemPersistencePort).saveAll(anyList());
  }

  @Test
  @DisplayName(
      "Given request items with unknown product codes when plan items are built then it rejects the operation as a conflict")
  void givenRequestItemsWithUnknownProductCodesWhenPlanItemsAreBuiltThenItRejectsTheOperationAsAConflict() {
    final Plan plan = new Plan("Plan Oro", "Cobertura completa", new BigDecimal("25.00"));
    final ItemPlanRequestDto request =
        ItemPlanRequestDto.builder()
            .item(ItemRequestPlanDto.builder().id(10L).name("Urna").code("URN-001").build())
            .quantity(2)
            .build();

    when(itemPersistencePort.findAllByCodeIn(List.of("URN-001"))).thenReturn(List.of());

    assertThatThrownBy(() -> service.buildItemsPlan(Set.of(request), plan))
        .isInstanceOf(ConflictException.class)
        .extracting("message")
        .isEqualTo("item.error.code.not.found");
  }

  @Test
  @DisplayName(
      "Given persisted plan items and a changed request set when deleted items are calculated then it returns only the items missing from the request")
  void givenPersistedPlanItemsAndAChangedRequestSetWhenDeletedItemsAreCalculatedThenItReturnsOnlyTheItemsMissingFromTheRequest() {
    final Plan plan = new Plan("Plan Oro", "Cobertura completa", new BigDecimal("25.00"));
    plan.setId(1L);
    final ItemEntity keptItem = new ItemEntity();
    keptItem.setId(10L);
    keptItem.setCode("URN-001");
    final ItemEntity removedItem = new ItemEntity();
    removedItem.setId(11L);
    removedItem.setCode("WRE-001");
    final ItemPlanEntity keptPlanItem = new ItemPlanEntity(plan, keptItem, 2);
    final ItemPlanEntity removedPlanItem = new ItemPlanEntity(plan, removedItem, 1);
    plan.setItemsPlan(new HashSet<>(Set.of(keptPlanItem, removedPlanItem)));
    final ItemPlanRequestDto request =
        ItemPlanRequestDto.builder()
            .item(ItemRequestPlanDto.builder().id(10L).name("Urna").code("URN-001").build())
            .quantity(2)
            .build();

    final List<ItemPlanEntity> deleted = service.getDeletedItemsPlanEntities(plan, Set.of(request));

    assertThat(deleted).containsExactly(removedPlanItem);
  }
}
