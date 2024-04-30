package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.IncomeTestDataFactory.getIncomeRequest;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import disenodesistemas.backendfunerariaapp.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.IncomeResponseDto;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.IncomeRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@ActiveProfiles("test")
@Sql(scripts = "/data-test.sql")
class IncomeServiceIntegrationTest {

  @Autowired private IncomeServiceImpl sut;
  @Autowired private IncomeRepository incomeRepository;
  private static final Long EXISTING_INCOME_RECEIPT_NUMBER = 20231108223102347L;
  private static final Long NON_EXISTING_INCOME_RECEIPT_NUM = 20241103223122343L;

  @Test
  void create() {
    final IncomeRequestDto incomeRequest = getIncomeRequest();
    final IncomeResponseDto actualResponse = sut.create(incomeRequest);
    assertEquals(2, incomeRepository.count());
    assertsIncome(incomeRequest, actualResponse);
  }

  @Test
  void findAll() {
    final List<IncomeResponseDto> expectedResult = incomeRepository.findAllByOrderByIdDesc();
    final List<IncomeResponseDto> actualResult = sut.findAll();

    assertAll(
        () -> assertEquals(expectedResult.size(), actualResult.size()),
        () ->
            assertEquals(
                expectedResult.get(0).getReceiptSeries(), actualResult.get(0).getReceiptSeries()),
        () ->
            assertEquals(
                expectedResult.get(0).getReceiptNumber(), actualResult.get(0).getReceiptNumber()),
        () ->
            assertEquals(
                expectedResult.get(0).getIncomeUser().getEmail(),
                actualResult.get(0).getIncomeUser().getEmail()),
        () ->
            assertEquals(
                expectedResult.get(0).getSupplier().getNif(),
                actualResult.get(0).getSupplier().getNif()),
        () ->
            assertEquals(
                expectedResult.get(0).getIncomeDate(), actualResult.get(0).getIncomeDate()));
  }

  @Test
  void update() {
    final IncomeRequestDto incomeRequest = getIncomeRequest();
    final IncomeResponseDto actualResponse =
        sut.update(EXISTING_INCOME_RECEIPT_NUMBER, incomeRequest);
    assertsIncome(incomeRequest, actualResponse);
    assertAll(
        () -> assertNotNull(actualResponse.getLastModifiedDate()),
        () -> assertNotNull(actualResponse.getLastModifiedBy()));
  }

  @Test
  void updateThrowsAnError() {
    final NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> sut.update(NON_EXISTING_INCOME_RECEIPT_NUM, getIncomeRequest()));

    assertAll(
        () -> assertEquals("income.error.not.found", exception.getMessage()),
        () -> assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatus().value()));
  }

  @Test
  void delete() {
    sut.delete(EXISTING_INCOME_RECEIPT_NUMBER);
    assertAll(
        () -> assertEquals(0, incomeRepository.count()),
        () ->
            assertFalse(
                incomeRepository.findByReceiptNumber(EXISTING_INCOME_RECEIPT_NUMBER).isPresent()));
  }

  @Test
  void testGetIncomesPaginated() {
    final int page = 1;
    final int limit = 1;
    final String sortBy = "id";
    final String sortDir = "asc";
    final Page<IncomeResponseDto> result = sut.getIncomesPaginated(page, limit, sortBy, sortDir);

    assertAll(
        () -> assertNotNull(result, "The result should not be null"),
        () ->
            assertEquals(
                limit, result.getContent().size(), "The size of the result should match the limit"),
        () ->
            assertEquals(
                page - 1,
                result.getNumber(),
                "The page number should be one less than the requested page (zero-based index)"));

    final Sort.Order sortOrder = result.getSort().getOrderFor(sortBy);
    assertAll(
        () -> assertNotNull(sortOrder, "Sort order for the sortBy field should not be null"),
        () ->
            assertEquals(
                sortDir.toUpperCase(),
                sortOrder.getDirection().name(),
                "The sorting direction should match the requested direction"));
  }

  @Test
  void findByReceiptNumber() {
    final IncomeResponseDto actualResponse =
        sut.findByReceiptNumber(EXISTING_INCOME_RECEIPT_NUMBER);

    assertAll(
        () -> assertNotNull(actualResponse),
        () ->
            assertEquals(
                EXISTING_INCOME_RECEIPT_NUMBER, Long.parseLong(actualResponse.getReceiptNumber())));
  }

  private void assertsIncome(
      final IncomeRequestDto incomeRequest, final IncomeResponseDto actualResponse) {
    assertAll(
        () ->
            assertEquals(
                incomeRequest.getSupplier().getName(), actualResponse.getSupplier().getName()),
        () ->
            assertEquals(
                incomeRequest.getIncomeUser().getEmail(),
                actualResponse.getIncomeUser().getEmail()),
        () ->
            assertEquals(
                incomeRequest.getReceiptType().getName(),
                actualResponse.getReceiptType().getName()),
        () -> assertEquals(incomeRequest.getTax(), actualResponse.getTax()),
        () ->
            assertEquals(
                incomeRequest.getIncomeDetails().size(), actualResponse.getIncomeDetails().size()),
        () -> assertNotNull(actualResponse.getTotalAmount()));
  }
}
