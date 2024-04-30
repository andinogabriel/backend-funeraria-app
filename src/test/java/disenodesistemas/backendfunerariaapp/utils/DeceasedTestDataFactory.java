package disenodesistemas.backendfunerariaapp.utils;

import static disenodesistemas.backendfunerariaapp.utils.GenderTestDataFactory.getFemaleGender;
import static disenodesistemas.backendfunerariaapp.utils.GenderTestDataFactory.getMaleGender;
import static disenodesistemas.backendfunerariaapp.utils.RelationshipTestDataFactory.getGrandMotherRelationship;
import static disenodesistemas.backendfunerariaapp.utils.RelationshipTestDataFactory.getGrandParentRelationship;
import static disenodesistemas.backendfunerariaapp.utils.UserTestDataFactory.getUserDto;

import disenodesistemas.backendfunerariaapp.dto.request.AddressRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.CityDto;
import disenodesistemas.backendfunerariaapp.dto.request.DeathCauseDto;
import disenodesistemas.backendfunerariaapp.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.ProvinceDto;
import java.time.LocalDate;
import java.time.Month;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DeceasedTestDataFactory {

  private static final String LAST_NAME = "Gomez";
  private static final String FIRST_NAME = "Alejandra";
  private static final Integer DNI = 18632946;
  private static final LocalDate BIRTH_DATE = LocalDate.of(1965, Month.JULY, 15);
  private static final LocalDate DEATH_DATE = LocalDate.of(2024, Month.NOVEMBER, 10);

  public static DeceasedRequestDto getDeceasedRequestDto() {
    return DeceasedRequestDto.builder()
        .lastName(LAST_NAME)
        .firstName(FIRST_NAME)
        .dni(DNI)
        .birthDate(BIRTH_DATE)
        .deathDate(DEATH_DATE)
        .placeOfDeath(getDeceasedPlaceOfDeath())
        .gender(getFemaleGender())
        .deceasedRelationship(getGrandMotherRelationship())
        .deathCause(getDeathCause())
        .user(getUserDto())
        .build();
  }

  public static DeceasedRequestDto getSavedInDbDeceasedRequestDto() {
    return DeceasedRequestDto.builder()
        .lastName("Perez")
        .firstName("Marta")
        .dni(DNI)
        .birthDate(BIRTH_DATE)
        .deathDate(DEATH_DATE)
        .placeOfDeath(getDeceasedPlaceOfDeath())
        .gender(getFemaleGender())
        .deceasedRelationship(getGrandMotherRelationship())
        .deathCause(getDeathCause())
        .user(getUserDto())
        .build();
  }

  public static DeceasedRequestDto getAffiliatedDeceasedRequestDto() {
    return DeceasedRequestDto.builder()
        .lastName("Acosta")
        .firstName("Juan")
        .dni(11236549)
        .birthDate(LocalDate.of(1950, Month.OCTOBER, 31))
        .deathDate(LocalDate.of(2024, Month.SEPTEMBER, 10))
        .placeOfDeath(getDeceasedPlaceOfDeath())
        .gender(getMaleGender())
        .deceasedRelationship(getGrandParentRelationship())
        .deathCause(getDeathCause())
        .user(getUserDto())
        .build();
  }

  public static DeceasedRequestDto getDeceasedExistingDniRequestDto() {
    return DeceasedRequestDto.builder()
        .lastName(LAST_NAME)
        .firstName(FIRST_NAME)
        .dni(22156961)
        .birthDate(BIRTH_DATE)
        .deathDate(DEATH_DATE)
        .placeOfDeath(getDeceasedPlaceOfDeath())
        .gender(getFemaleGender())
        .deceasedRelationship(getGrandMotherRelationship())
        .deathCause(getDeathCause())
        .user(getUserDto())
        .build();
  }

  private static DeathCauseDto getDeathCause() {
    return DeathCauseDto.builder().id(2L).name("'Muerte clínica").build();
  }

  private static AddressRequestDto getDeceasedPlaceOfDeath() {
    return AddressRequestDto.builder()
        .streetName("Belgrano")
        .blockStreet(500)
        .city(
            CityDto.builder()
                .id(7871L)
                .name("PRESIDENCIA ROQUE SAENZ PEÑA")
                .province(ProvinceDto.builder().id(16L).name("Chaco").code31662("AR-H").build())
                .build())
        .build();
  }
}
