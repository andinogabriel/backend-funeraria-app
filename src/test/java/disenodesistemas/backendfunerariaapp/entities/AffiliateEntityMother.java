package disenodesistemas.backendfunerariaapp.entities;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.Month;

@UtilityClass
public class AffiliateEntityMother {

  private static final Integer DNI = 12345678;
  private static final String FIRST_NAME = "John";
  private static final String LAST_NAME = "Doe";
  private static final LocalDate BIRTH_DATE = LocalDate.of(1980, Month.APRIL, 30);
  private static final LocalDate START_DATE = LocalDate.of(2023, Month.SEPTEMBER, 20);

  public static AffiliateEntity getAffiliateEntity() {
    return AffiliateEntity.builder()
        .id(1L)
        .firstName(FIRST_NAME)
        .lastName(LAST_NAME)
        .birthDate(BIRTH_DATE)
        .dni(DNI)
        .startDate(START_DATE)
        .deceased(Boolean.FALSE)
        .gender(GenderEntityMother.getMaleGender())
        .user(UserEntityMother.getUser())
        .relationship(RelationshipEntityMother.getParentRelationship())
        .build();
  }
}
