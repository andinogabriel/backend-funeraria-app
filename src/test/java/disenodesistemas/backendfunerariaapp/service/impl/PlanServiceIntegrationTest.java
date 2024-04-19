package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDtoMother.getInvalidPlanRequest;
import static disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDtoMother.getInvalidPlanRequestItemWithoutPrice;
import static disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDtoMother.getPlanRequest;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.PlanResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.entities.Plan;
import disenodesistemas.backendfunerariaapp.exceptions.ConflictException;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import disenodesistemas.backendfunerariaapp.repository.ItemsPlanRepository;
import disenodesistemas.backendfunerariaapp.repository.PlanRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@ActiveProfiles("test")
@Sql(scripts = "/data-test.sql")
class PlanServiceIntegrationTest {

  @Autowired private PlanRepository planRepository;
  @Autowired private ItemsPlanRepository itemsPlanRepository;
  @Autowired private PlanServiceImpl sut;
  @Autowired private ItemRepository itemRepository;

  private static final Long EXISTING_PLAN_ID = 1L;
  private static final String EXISTING_PLAN_NAME_1 = "Plan Simple";
  private static final String EXISTING_PLAN_NAME_2 = "Plan nivel medio";

  @AfterEach
  void tearDown() {
    planRepository.deleteAll();
    itemsPlanRepository.deleteAll();
    planRepository.flush();
    itemsPlanRepository.flush();
  }

  @DisplayName(
      "Given a valid plan request when create method is called then it returns a created plan response dto")
  @Test
  void create() {
    final PlanRequestDto planRequestDto = getPlanRequest();
    final PlanResponseDto actualPlanCreated = sut.create(planRequestDto);

    assertAll(
        () -> assertPlanResponseDto(actualPlanCreated, planRequestDto),
        () -> assertEquals(3, planRepository.count()));
  }

  @DisplayName(
      "Given an invalid plan request with an item without price when create method is called then it throws a ConflictException")
  @Test
  void createThrowsError() {
    final ConflictException exception =
        assertThrows(
            ConflictException.class, () -> sut.create(getInvalidPlanRequestItemWithoutPrice()));

    assertNotNull(exception.getMessage());
    assertEquals("plan.error.price.calculator", exception.getMessage());
  }

  @DisplayName(
      "Given a existing plan identifier when delete method is called then it delete the plan successfully")
  @Test
  void delete() {
    sut.delete(EXISTING_PLAN_ID);

    assertFalse(planRepository.existsById(EXISTING_PLAN_ID), "Plan should be deleted");
  }

  @Test
  @DisplayName(
      "Given a plan request dto with an item with code not found when call create method is called then it throws an IllegalArgumentException")
  void createThrowsAnError() {
    final IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> sut.create(getInvalidPlanRequest()));
    assertNotNull(exception.getMessage());
    assertTrue(exception.getMessage().contains("not found"));
  }

  @DisplayName(
      "Given a valid plan request dto when update method is called then it returns the updated plan response dto")
  @Test
  void update() {
    final PlanRequestDto updatePlanRequest = getPlanRequest();
    final PlanResponseDto actualPlanUpdated = sut.update(EXISTING_PLAN_ID, updatePlanRequest);

    assertAll(
        () -> assertPlanResponseDto(actualPlanUpdated, updatePlanRequest),
        () -> assertEquals(2, planRepository.count()));
  }

  @DisplayName("When find all methods is called then it returns all plan response dto")
  @Test
  void findAll() {
    final List<PlanResponseDto> result = sut.findAll();

    assertAll(
        () -> assertEquals(EXISTING_PLAN_NAME_1, result.get(1).getName()),
        () -> assertEquals(EXISTING_PLAN_NAME_2, result.get(0).getName()),
        () -> assertEquals(2, result.size(), "Number of retrieved plans should be 2"));
  }

  @DisplayName(
      "Given the price update of any existing item in the db when the update method of the article service is called then updatePlansPrice method of PlanService is called")
  @Test
  void updatePlansPrice() {
    final List<ItemEntity> itemsToUpdate = (List<ItemEntity>) itemRepository.findAll();

    sut.updatePlansPrice(itemsToUpdate);

    final List<Plan> updatedPlans = planRepository.findAllById(List.of(1L, 2L));

    final BigDecimal expectedPricePlan1 =
        BigDecimal.valueOf(3300.00).setScale(2, RoundingMode.HALF_EVEN);
    final BigDecimal expectedPricePlan2 =
        BigDecimal.valueOf(14950.00).setScale(2, RoundingMode.HALF_EVEN);

    assertEquals(expectedPricePlan1, updatedPlans.get(0).getPrice());
    assertEquals(expectedPricePlan2, updatedPlans.get(1).getPrice());
  }

  @DisplayName(
      "Given an existing plan identifier when getById method is called then it returns a plan response dto")
  @Test
  void getById() {
    PlanResponseDto actualResponse = sut.getById(EXISTING_PLAN_ID);
    assertNotNull(actualResponse);
    assertEquals(EXISTING_PLAN_ID, actualResponse.getId());
    assertEquals(EXISTING_PLAN_NAME_1, actualResponse.getName());
  }

  private void assertPlanResponseDto(final PlanResponseDto actual, final PlanRequestDto expected) {
    assertNotNull(actual.getId());
    assertEquals(expected.getDescription(), actual.getDescription());
    assertEquals(expected.getProfitPercentage(), actual.getProfitPercentage());
    assertEquals(new BigDecimal("6600.00"), actual.getPrice());
    assertEquals(expected.getName(), actual.getName());
    assertFalse(actual.getItemsPlan().isEmpty(), "Items Plan Set should not be empty");
  }
}
