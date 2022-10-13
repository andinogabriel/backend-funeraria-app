package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.ItemPlanRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDtoMother;
import disenodesistemas.backendfunerariaapp.dto.response.PlanResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ItemPlanEntity;
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

import static disenodesistemas.backendfunerariaapp.entities.ItemPlanEntityMother.getItemPlanEntity;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemsPlanRepository itemsPlanRepository;
    @Mock
    private ProjectionFactory projectionFactory;
    @Mock
    private AbstractConverter<ItemPlanEntity, ItemPlanRequestDto> itemPlanConverter;

    @InjectMocks
    private PlanServiceImpl sut;

    private ProjectionFactory factory;


    @BeforeEach
    void setUp() {
        factory = new SpelAwareProxyProjectionFactory();
    }

    @Test
    void create() {
        final Plan planEntity = PlanEntityMother.getPlan();
        planEntity.setId(1L);
        given(planRepository.save(PlanEntityMother.getPlan())).willReturn(planEntity);
        given(itemsPlanRepository.save(getItemPlanEntity())).willReturn(getItemPlanEntity());
        final PlanResponseDto planResponseDto = factory.createProjection(PlanResponseDto.class, planEntity);
        given(projectionFactory.createProjection(PlanResponseDto.class, planEntity)).willReturn(planResponseDto);
        final PlanResponseDto response = sut.create(PlanRequestDtoMother.getPlanRequest());
        System.out.println(response);
        verify(planRepository, times(2)).save(any(Plan.class));
    }
}