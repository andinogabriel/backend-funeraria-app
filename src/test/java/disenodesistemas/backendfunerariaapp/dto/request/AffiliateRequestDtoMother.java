package disenodesistemas.backendfunerariaapp.dto.request;

import disenodesistemas.backendfunerariaapp.dto.GenderDtoMother;
import disenodesistemas.backendfunerariaapp.dto.RelationshipDtoMother;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.Month;

@UtilityClass
public class AffiliateRequestDtoMother {

  public static AffiliateRequestDto getAffiliateRequestDto() {
    return AffiliateRequestDto.builder()
        .dni(12345678)
        .gender(GenderDtoMother.getMaleGender())
        .relationship(RelationshipDtoMother.getParentRelationship())
        .lastName("Doe")
        .firstName("John")
        .birthDate(LocalDate.of(1980, Month.APRIL, 30))
        .build();
  }

  public static AffiliateRequestDto getAffiliateRequestDtoDifferentDni() {
    return AffiliateRequestDto.builder()
        .dni(32165456)
        .gender(GenderDtoMother.getMaleGender())
        .relationship(RelationshipDtoMother.getParentRelationship())
        .lastName("Doe")
        .firstName("John")
        .birthDate(LocalDate.of(1980, Month.APRIL, 30))
        .build();
  }
}
