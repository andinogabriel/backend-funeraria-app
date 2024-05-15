package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.utils.AffiliateTestDataFactory.getAffiliateEntity;
import static disenodesistemas.backendfunerariaapp.utils.AffiliateTestDataFactory.getAffiliateRequestDto;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.AffiliateResponseDto;
import disenodesistemas.backendfunerariaapp.entities.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.service.AffiliateService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class AffiliateControllerTest
    extends AbstractControllerTest<
        AffiliateRequestDto, AffiliateResponseDto, AffiliateEntity, Integer> {

  @Mock private AffiliateService affiliateService;
  @InjectMocks private AffiliateController sut;
  private static final Integer VALID_DNI = 123456789;
  private static final String SEARCH_VALUE = "searchValue";

  @Test
  @DisplayName(
      "Given a valid affiliate request when create method is called then save the new affiliate in the db and returns a new affiliate response")
  void createAffiliate() {
    testCreate(affiliateService::create, sut::create, requestDto, responseDto);
    then(affiliateService).should(times(1)).create(requestDto);
  }

  @Test
  @DisplayName(
      "Given a string value in query parameter when call findAffiliatesByFirstNameOrLastNameOrDniContaining method then return a list of all the affiliates that contains the given string value")
  void findAffiliatesByFirstNameOrLastNameOrDniContaining() {
    testFindAll(
            () -> List.of(responseDto),
            () -> sut.findAffiliatesByFirstNameOrLastNameOrDniContaining(SEARCH_VALUE),
            () -> List.of(responseDto),
            () -> affiliateService.findAffiliatesByFirstNameOrLastNameOrDniContaining(anyString()));
    then(affiliateService)
        .should(times(1))
        .findAffiliatesByFirstNameOrLastNameOrDniContaining(SEARCH_VALUE);
  }

  @Test
  @DisplayName(
      "When findAllByDeceasedFalse method is called then returns all affiliates that deceased field is false")
  void findAllByDeceasedFalse() {
    testFindAll(
        () -> List.of(responseDto),
        sut::findAllByDeceasedFalse,
        () -> List.of(responseDto),
        affiliateService::findAllByDeceasedFalse);
    then(affiliateService).should(times(1)).findAllByDeceasedFalse();
  }

  @Test
  @DisplayName("When findAll method is called then returns all affiliates")
  void findAll() {
    testFindAll(
        () -> List.of(responseDto),
        sut::findAll,
        () -> List.of(responseDto),
        affiliateService::findAll);
    then(affiliateService).should(times(1)).findAll();
  }

  @Test
  @DisplayName(
      "Given the logged user when findAffiliatesByUser method is called then return a list of all affiliates by that user")
  void findAffiliatesByUser() {
    testFindAll(
        () -> List.of(responseDto),
        sut::findAffiliatesByUser,
        () -> List.of(responseDto),
        affiliateService::findAffiliatesByUser);

    then(affiliateService).should(times(1)).findAffiliatesByUser();
  }

  @Test
  @DisplayName(
      "Given a valid dni when call delete method then affiliate is deleted from the database")
  void deleteAffiliate() {
    testDelete(sut::delete, VALID_DNI, "DELETE AFFILIATE");
    then(affiliateService).should(times(1)).delete(VALID_DNI);
  }

  @Test
  @DisplayName(
      "Given a valid dni and affiliate request when update method is called then returns the affiliate updated successfully")
  void updateAffiliate() {
    testUpdate(affiliateService::update, sut::update, VALID_DNI, requestDto, responseDto);
    then(affiliateService).should(times(1)).update(VALID_DNI, requestDto);
  }

  @Override
  protected AffiliateRequestDto getRequestDto() {
    return getAffiliateRequestDto();
  }

  @Override
  protected Class<AffiliateResponseDto> getResponseDtoClass() {
    return AffiliateResponseDto.class;
  }

  @Override
  protected AffiliateEntity getEntity() {
    return getAffiliateEntity();
  }
}
