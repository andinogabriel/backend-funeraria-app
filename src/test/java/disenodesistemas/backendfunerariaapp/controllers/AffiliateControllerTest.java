package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.utils.AffiliateTestDataFactory.getAffiliateEntity;
import static disenodesistemas.backendfunerariaapp.utils.AffiliateTestDataFactory.getAffiliateRequestDto;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import disenodesistemas.backendfunerariaapp.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.AffiliateResponseDto;
import disenodesistemas.backendfunerariaapp.service.AffiliateService;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AffiliateControllerTest {

  @Mock private AffiliateService affiliateService;

  @InjectMocks private AffiliateController affiliateController;
  private static final Integer VALID_DNI = 123456789;
  private static final String SEARCH_VALUE = "searchValue";
  private AffiliateRequestDto affiliateRequest;
  private AffiliateResponseDto affiliateResponseDto;

  @BeforeEach
  void setUp() {
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    this.affiliateRequest = getAffiliateRequestDto();
    this.affiliateResponseDto =
        projectionFactory.createProjection(AffiliateResponseDto.class, getAffiliateEntity());
  }

  @Test
  @DisplayName(
      "Given a valid affiliate request when create method is called then save the new affiliate in the db and returns a new affiliate response")
  void createAffiliate() {
    given(affiliateService.create(affiliateRequest)).willReturn(affiliateResponseDto);

    final ResponseEntity<AffiliateResponseDto> actualResponse =
        affiliateController.createAffiliate(affiliateRequest);

    assertAll(
        () -> assertEquals(HttpStatus.CREATED, actualResponse.getStatusCode()),
        () -> assertEquals(affiliateResponseDto, actualResponse.getBody()));

    verify(affiliateService, only()).create(affiliateRequest);
  }

  @Test
  @DisplayName(
      "Given a string value in query parameter when call findAffiliatesByFirstNameOrLastNameOrDniContaining method then return a list of all the affiliates that contains the given string value")
  void findAffiliatesByFirstNameOrLastNameOrDniContaining() {
    final List<AffiliateResponseDto> actualResponse = List.of(affiliateResponseDto);
    given(affiliateService.findAffiliatesByFirstNameOrLastNameOrDniContaining(anyString()))
        .willReturn(actualResponse);

    final ResponseEntity<List<AffiliateResponseDto>> responseEntity =
        affiliateController.findAffiliatesByFirstNameOrLastNameOrDniContaining(SEARCH_VALUE);

    assertAll(
        () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
        () -> assertEquals(actualResponse, responseEntity.getBody()));
    verify(affiliateService, only())
        .findAffiliatesByFirstNameOrLastNameOrDniContaining(SEARCH_VALUE);
  }

  @Test
  @DisplayName(
      "When findAllByDeceasedFalse method is called then returns all affiliates that deceased field is false")
  void findAllByDeceasedFalse() {
    final List<AffiliateResponseDto> actualResponse = List.of(affiliateResponseDto);
    given(affiliateService.findAllByDeceasedFalse()).willReturn(actualResponse);

    final ResponseEntity<List<AffiliateResponseDto>> responseEntity =
        affiliateController.findAllByDeceasedFalse();

    assertAll(
        () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
        () -> assertEquals(actualResponse, responseEntity.getBody()));
    verify(affiliateService, only()).findAllByDeceasedFalse();
  }

  @Test
  @DisplayName("When findAll method is called then returns all affiliates")
  void findAll() {
    final List<AffiliateResponseDto> actualResponse = List.of(affiliateResponseDto);
    given(affiliateService.findAll()).willReturn(actualResponse);

    final ResponseEntity<List<AffiliateResponseDto>> responseEntity = affiliateController.findAll();

    assertAll(
        () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
        () -> assertEquals(actualResponse, responseEntity.getBody()));
    verify(affiliateService, only()).findAll();
  }

  @Test
  @DisplayName(
      "Given the logged user when findAffiliatesByUser method is called then return a list of all affiliates by that user")
  void findAffiliatesByUser() {
    final List<AffiliateResponseDto> expectedResponse = List.of(affiliateResponseDto);
    given(affiliateService.findAffiliatesByUser()).willReturn(expectedResponse);

    final ResponseEntity<List<AffiliateResponseDto>> actualResponse =
        affiliateController.findAffiliatesByUser();

    assertAll(
        () -> assertEquals(HttpStatus.OK, actualResponse.getStatusCode()),
        () -> assertEquals(expectedResponse, actualResponse.getBody()));
    verify(affiliateService, only()).findAffiliatesByUser();
  }

  @Test
  @DisplayName(
      "Given a valid dni when call delete method then affiliate is deleted from the database")
  void deleteAffiliate() {
    final ResponseEntity<OperationStatusModel> expectedResponse =
        affiliateController.deleteAffiliate(VALID_DNI);

    assertAll(
        () -> assertEquals(HttpStatus.OK, expectedResponse.getStatusCode()),
        () -> assertEquals("DELETE AFFILIATE", expectedResponse.getBody().getName()),
        () -> assertEquals("SUCCESS", expectedResponse.getBody().getResult()));
    verify(affiliateService, only()).delete(VALID_DNI);
  }

  @Test
  @DisplayName(
      "Given a valid dni and affiliate request when update method is called then returns the affiliate updated successfully")
  void updateAffiliate() {
    given(affiliateService.update(VALID_DNI, affiliateRequest)).willReturn(affiliateResponseDto);

    final ResponseEntity<AffiliateResponseDto> actualResponse =
        affiliateController.updateAffiliate(VALID_DNI, affiliateRequest);

    assertAll(
        () -> assertEquals(HttpStatus.OK, actualResponse.getStatusCode()),
        () -> assertEquals(affiliateResponseDto, actualResponse.getBody()));
    verify(affiliateService, only()).update(VALID_DNI, affiliateRequest);
  }
}
