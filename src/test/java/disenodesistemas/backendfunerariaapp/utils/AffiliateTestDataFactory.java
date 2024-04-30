package disenodesistemas.backendfunerariaapp.utils;

import static disenodesistemas.backendfunerariaapp.utils.GenderTestDataFactory.getEntityMaleGender;
import static disenodesistemas.backendfunerariaapp.utils.RelationshipTestDataFactory.getParentRelationshipDto;

import disenodesistemas.backendfunerariaapp.dto.request.AffiliateRequestDto;
import java.time.LocalDate;
import java.time.Month;

import disenodesistemas.backendfunerariaapp.entities.AffiliateEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AffiliateTestDataFactory {

  private static final Integer DNI = 12345678;
  private static final String FIRST_NAME = "John";
  private static final String LAST_NAME = "Doe";
  private static final LocalDate BIRTH_DATE = LocalDate.of(1980, Month.APRIL, 30);
  private static final LocalDate START_DATE = LocalDate.of(2023, Month.SEPTEMBER, 20);

  public static AffiliateRequestDto getAffiliateRequestDto() {
    return AffiliateRequestDto.builder()
        .dni(DNI)
        .gender(GenderTestDataFactory.getMaleGender())
        .relationship(getParentRelationshipDto())
        .lastName(LAST_NAME)
        .firstName(FIRST_NAME)
        .birthDate(BIRTH_DATE)
        .build();
  }

  public static AffiliateRequestDto getAffiliateRequestDtoDifferentDni() {
    return AffiliateRequestDto.builder()
        .dni(32165456)
        .gender(GenderTestDataFactory.getMaleGender())
        .relationship(getParentRelationshipDto())
        .lastName(LAST_NAME)
        .firstName(FIRST_NAME)
        .birthDate(BIRTH_DATE)
        .build();
  }

  public static AffiliateEntity getAffiliateEntity() {
    return AffiliateEntity.builder()
        .id(1L)
        .firstName(FIRST_NAME)
        .lastName(LAST_NAME)
        .birthDate(BIRTH_DATE)
        .dni(DNI)
        .startDate(START_DATE)
        .deceased(Boolean.FALSE)
        .gender(getEntityMaleGender())
        .user(UserTestDataFactory.getUserEntity())
        .relationship(RelationshipTestDataFactory.getParentRelationship())
        .build();
  }
}
