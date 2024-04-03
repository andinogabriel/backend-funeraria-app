package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.ItemPlanRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDtoMother;
import disenodesistemas.backendfunerariaapp.dto.response.PlanResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ItemEntityMother;
import disenodesistemas.backendfunerariaapp.entities.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemPlanEntityMother;
import disenodesistemas.backendfunerariaapp.entities.Plan;
import disenodesistemas.backendfunerariaapp.entities.PlanEntityMother;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import disenodesistemas.backendfunerariaapp.repository.ItemsPlanRepository;
import disenodesistemas.backendfunerariaapp.repository.PlanRepository;
import disenodesistemas.backendfunerariaapp.service.converters.AbstractConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class PlanServiceTest {

  @Mock private PlanRepository planRepository;
  @Mock private ItemRepository itemRepository;
  @Mock private ItemsPlanRepository itemsPlanRepository;
  @Mock private ProjectionFactory projectionFactory;
  @Mock private AbstractConverter<ItemPlanEntity, ItemPlanRequestDto> itemPlanConverter;

  @InjectMocks private PlanServiceImpl sut;

  private PlanResponseDto planResponseDto;

  @BeforeEach
  void setUp() {
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    planResponseDto =
        projectionFactory.createProjection(PlanResponseDto.class, PlanEntityMother.getPlan());
  }

  @Test
  void create() {
    final Plan planEntity = PlanEntityMother.getPlan();

    given(itemRepository.findAllByCodeIn(anyList()))
        .willReturn(List.of(ItemEntityMother.getItem()));
    given(itemsPlanRepository.save(any(ItemPlanEntity.class)))
        .willReturn(ItemPlanEntityMother.getItemPlanEntity());
    given(planRepository.save(any(Plan.class))).willReturn(planEntity);
    given(projectionFactory.createProjection(PlanResponseDto.class, planEntity))
        .willReturn(planResponseDto);

    final PlanResponseDto response = sut.create(PlanRequestDtoMother.getPlanRequest());

    assertAll(
        () -> assertEquals(planEntity.getName(), response.getName()),
        () -> assertEquals(planEntity.getDescription(), response.getDescription()),
        () -> assertEquals(planEntity.getProfitPercentage(), response.getProfitPercentage()),
        () -> assertEquals(planEntity.getItemsPlan().size(), response.getItemsPlan().size()),
        () -> assertEquals(planEntity.getPrice(), response.getPrice()));

    verify(planRepository, times(2)).save(any(Plan.class));
  }
}
