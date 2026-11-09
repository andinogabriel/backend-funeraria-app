package disenodesistemas.backendfunerariaapp.modern.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.mapping.AffiliateMapper;
import disenodesistemas.backendfunerariaapp.mapping.AffiliateMapperImpl;
import disenodesistemas.backendfunerariaapp.web.dto.request.AffiliateRequestDto;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The enrolment date drives the claim waiting period (ADR-0022), so the generated mapper must copy
 * it when sent and never wipe the stored one when an edit omits it (the column is NOT NULL).
 */
class AffiliateMapperEnrolmentDateTest {

  private final AffiliateMapper mapper = new AffiliateMapperImpl();

  @Test
  @DisplayName("Given an edit without enrolment date when mapping onto the entity then the stored date is kept")
  void omittedEnrolmentDateKeepsStoredOne() {
    final LocalDate stored = LocalDate.of(2024, 2, 1);
    final AffiliateEntity entity = AffiliateEntity.builder().enrolmentDate(stored).build();

    mapper.updateEntity(AffiliateRequestDto.builder().firstName("Ana").build(), entity);

    assertThat(entity.getEnrolmentDate()).isEqualTo(stored);
    assertThat(entity.getFirstName()).isEqualTo("Ana");
  }

  @Test
  @DisplayName("Given an edit with a new enrolment date when mapping onto the entity then it replaces the stored one")
  void sentEnrolmentDateReplacesStoredOne() {
    final AffiliateEntity entity =
        AffiliateEntity.builder().enrolmentDate(LocalDate.of(2024, 2, 1)).build();

    mapper.updateEntity(
        AffiliateRequestDto.builder().enrolmentDate(LocalDate.of(2023, 6, 1)).build(), entity);

    assertThat(entity.getEnrolmentDate()).isEqualTo(LocalDate.of(2023, 6, 1));
    assertThat(mapper.toDto(entity).enrolmentDate()).isEqualTo(LocalDate.of(2023, 6, 1));
  }
}
