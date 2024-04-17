package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDtoMother.getInvalidPlanRequest;
import static disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDtoMother.getPlanRequest;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.PlanResponseDto;
import disenodesistemas.backendfunerariaapp.repository.ItemsPlanRepository;
import disenodesistemas.backendfunerariaapp.repository.PlanRepository;
import java.math.BigDecimal;
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

  private static final Long EXISTING_PLAN_ID = 1L;
  private static final String EXISTING_PLAN_NAME_1 = "Plan Simple";
  private static final String EXISTING_PLAN_NAME_2 = "Plan nivel medio";

  @AfterEach
  void tearDown() {
    planRepository.deleteAll();
    itemsPlanRepository.deleteAll();
  }

  @Test
  void create() {
    final PlanRequestDto planRequestDto = getPlanRequest();
    final PlanResponseDto actualPlanCreated = sut.create(planRequestDto);

    assertAll(
        () -> assertPlanResponseDto(actualPlanCreated, planRequestDto),
        () -> assertEquals(3, planRepository.count()));
  }

  @Test
  void delete() {
    sut.delete(EXISTING_PLAN_ID);

    assertFalse(planRepository.existsById(EXISTING_PLAN_ID), "Plan should be deleted");
  }

  @Test
  @DisplayName(
      "Given a plan request dto with items with code not found when call create method is called then it throws an exception")
  void createThrowsAnError() {
    final IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> sut.create(getInvalidPlanRequest()));
    assertNotNull(exception.getMessage());
    assertTrue(exception.getMessage().contains("not found"));
    assertEquals(0, itemsPlanRepository.count());
  }

  @Test
  void update() {
    final PlanRequestDto updatePlanRequest = getPlanRequest();
    final PlanResponseDto actualPlanUpdated = sut.update(EXISTING_PLAN_ID, updatePlanRequest);

    assertAll(
        () -> assertPlanResponseDto(actualPlanUpdated, updatePlanRequest),
        () -> assertEquals(2, planRepository.count()));
  }

  @Test
  void findAll() {
    final List<PlanResponseDto> result = sut.findAll();

    assertAll(
        () -> assertEquals(EXISTING_PLAN_NAME_1, result.get(1).getName()),
        () -> assertEquals(EXISTING_PLAN_NAME_2, result.get(0).getName()),
        () -> assertEquals(2, result.size(), "Number of retrieved plans should be 2"));
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
