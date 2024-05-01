package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.FuneralTestDataFactory.getFuneralExistingDeceasedDniRequestDto;
import static disenodesistemas.backendfunerariaapp.utils.FuneralTestDataFactory.getFuneralExistingReceiptNumberRequestDto;
import static disenodesistemas.backendfunerariaapp.utils.FuneralTestDataFactory.getFuneralRequestDto;
import static disenodesistemas.backendfunerariaapp.utils.FuneralTestDataFactory.getFuneralRequestDtoAffiliatedDeceased;
import static disenodesistemas.backendfunerariaapp.utils.FuneralTestDataFactory.getSavedInDBFuneralRequestDto;
import static disenodesistemas.backendfunerariaapp.utils.FuneralTestDataFactory.getSavedInDBFuneralRequestDtoThrowsException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import disenodesistemas.backendfunerariaapp.dto.request.FuneralRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.FuneralResponseDto;
import disenodesistemas.backendfunerariaapp.exceptions.ConflictException;
import disenodesistemas.backendfunerariaapp.repository.FuneralRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@ActiveProfiles("test")
@Sql(scripts = "/data-test.sql")
class FuneralServiceIntegrationTest {

  @Autowired private FuneralServiceImpl sut;
  @Autowired private FuneralRepository funeralRepository;
  private static FuneralRequestDto funeralRequestDto;
  private static final Long EXISTING_FUNERAL_ID = 45L;
  private static final String EXISTING_RECEIPT_NUMBER = "123465sad465";
  private static final String EXISTING_RECEIPT_SERIES = "465asd4as";

  @BeforeEach
  void setUp() {
    funeralRequestDto = getFuneralRequestDto();
    final Authentication authentication =
        new UsernamePasswordAuthenticationToken("email_test@gmail.com", "asdPassword123");
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Test
  void create() {
    final FuneralResponseDto actualResponse = sut.create(funeralRequestDto);
    funeralAsserts(actualResponse);
    assertEquals(3, funeralRepository.count());
  }

  @Test
  void createWithAnAffiliatedDeceased() {
    funeralRequestDto = getFuneralRequestDtoAffiliatedDeceased();
    final FuneralResponseDto actualResponse = sut.create(funeralRequestDto);
    funeralAsserts(actualResponse);
    assertAll(
        () -> assertTrue(actualResponse.getDeceased().getAffiliated()),
        () -> assertEquals(3, funeralRepository.count()));
  }

  @Test
  void createThrowsExceptionReceiptNumberAlreadyExists() {
    funeralRequestDto = getFuneralExistingReceiptNumberRequestDto();
    final ConflictException exception =
        assertThrows(ConflictException.class, () -> sut.create(funeralRequestDto));

    assertAll(
        () -> assertEquals(HttpStatus.CONFLICT, exception.getStatus()),
        () -> assertEquals("funeral.error.receiptNumber.already.exists", exception.getMessage()));
  }

  @Test
  void createThrowsExceptionDeceasedDniAlreadyExists() {
    funeralRequestDto = getFuneralExistingDeceasedDniRequestDto();
    final ConflictException exception =
        assertThrows(ConflictException.class, () -> sut.create(funeralRequestDto));

    assertAll(
        () -> assertEquals(HttpStatus.CONFLICT, exception.getStatus()),
        () -> assertEquals("funeral.error.deceased.dni.already.exists", exception.getMessage()));
  }

  @Test
  void update() {
    funeralRequestDto = getSavedInDBFuneralRequestDto();
    final FuneralResponseDto actualResponse = sut.update(EXISTING_FUNERAL_ID, funeralRequestDto);
    funeralAsserts(actualResponse);
    assertAll(
        () -> assertEquals(2, funeralRepository.count()),
        () -> assertTrue(funeralRepository.findById(EXISTING_FUNERAL_ID).isPresent()));
  }

  @Test
  void updateThrowsConflictException() {
    funeralRequestDto = getSavedInDBFuneralRequestDtoThrowsException();

    final ConflictException exception =
        assertThrows(
            ConflictException.class, () -> sut.update(EXISTING_FUNERAL_ID, funeralRequestDto));

    assertAll(
        () -> assertEquals(HttpStatus.CONFLICT, exception.getStatus()),
        () -> assertEquals("funeral.error.receiptNumber.already.exists", exception.getMessage()));
  }

  @Test
  void delete() {
    sut.delete(EXISTING_FUNERAL_ID);
    assertAll(
        () -> assertEquals(1, funeralRepository.count()),
        () -> assertFalse(funeralRepository.findById(EXISTING_FUNERAL_ID).isPresent()));
  }

  @Test
  void findById() {
    final FuneralResponseDto actualResponse = sut.findById(EXISTING_FUNERAL_ID);
    assertAll(
        () -> assertEquals(EXISTING_FUNERAL_ID, actualResponse.getId()),
        () -> assertEquals(EXISTING_RECEIPT_NUMBER, actualResponse.getReceiptNumber()),
        () -> assertEquals(EXISTING_RECEIPT_SERIES, actualResponse.getReceiptSeries()));
  }

  @Test
  void findAll() {
    final List<FuneralResponseDto> actualResponse = sut.findAll();
    assertAll(
        () -> assertFalse(actualResponse.isEmpty()),
        () -> assertEquals(EXISTING_FUNERAL_ID, actualResponse.get(0).getId()),
        () -> assertEquals(EXISTING_RECEIPT_SERIES, actualResponse.get(0).getReceiptSeries()),
        () -> assertEquals(EXISTING_RECEIPT_NUMBER, actualResponse.get(0).getReceiptNumber()));
  }

  @Test
  void findFuneralsByUser() {
    final List<FuneralResponseDto> actualResponse = sut.findFuneralsByUser();
    final String loggedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
    assertAll(
        () ->
            assertEquals(
                loggedUserEmail, actualResponse.get(1).getDeceased().getDeceasedUser().getEmail()),
        () -> assertFalse(actualResponse.isEmpty()),
        () -> assertEquals(EXISTING_FUNERAL_ID, actualResponse.get(1).getId()),
        () -> assertEquals(EXISTING_RECEIPT_SERIES, actualResponse.get(1).getReceiptSeries()),
        () -> assertEquals(EXISTING_RECEIPT_NUMBER, actualResponse.get(1).getReceiptNumber()));
  }

  private void funeralAsserts(final FuneralResponseDto actualResponse) {
    assertAll(
        () -> assertEquals(funeralRequestDto.getFuneralDate(), actualResponse.getFuneralDate()),
        () -> assertEquals(funeralRequestDto.getTax(), actualResponse.getTax()),
        () -> assertNotNull(actualResponse.getReceiptSeries(), "Receipt series could not be null"),
        () -> assertNotNull(actualResponse.getReceiptNumber(), "Receipt number could not be null"),
        () -> assertNotNull(actualResponse.getRegisterDate(), "Register date could not be null"),
        () ->
            assertNotNull(actualResponse.getDeceased().getDni(), "Deceased dni could not be null"),
        () ->
            assertTrue(
                actualResponse.getTotalAmount().compareTo(BigDecimal.ZERO) > 0,
                "Total amount must be greater than zero"),
        () ->
            assertEquals(
                funeralRequestDto.getDeceased().getBirthDate(),
                actualResponse.getDeceased().getBirthDate()),
        () ->
            assertEquals(
                funeralRequestDto.getDeceased().getFirstName(),
                actualResponse.getDeceased().getFirstName()),
        () ->
            assertEquals(
                funeralRequestDto.getDeceased().getLastName(),
                actualResponse.getDeceased().getLastName()),
        () ->
            assertEquals(
                funeralRequestDto.getReceiptType().getName(),
                actualResponse.getReceiptType().getName()),
        () ->
            assertEquals(
                funeralRequestDto.getPlan().getName(), actualResponse.getPlan().getName()));
  }
}
