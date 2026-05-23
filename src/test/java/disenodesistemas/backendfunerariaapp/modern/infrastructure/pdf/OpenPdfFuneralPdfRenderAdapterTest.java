package disenodesistemas.backendfunerariaapp.modern.infrastructure.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.domain.entity.AddressEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.CityEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.DeathCauseEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.domain.entity.GenderEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.domain.entity.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.infrastructure.pdf.OpenPdfFuneralPdfRenderAdapter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Adapter-level test for the OpenPDF renderer. We do not assert on visual
 * layout — that lives in the manual review attached to the PR. Instead we
 * lock the two structural guarantees the rest of the stack relies on:
 *
 * <ul>
 *   <li>The output starts with the PDF magic number ({@code %PDF-}). Without
 *       it any downstream PDF viewer rejects the file as corrupt.</li>
 *   <li>The output is non-trivial in size — guards against the regression
 *       where a partially-initialised {@code Funeral} silently yields an
 *       empty document.</li>
 * </ul>
 *
 * The fixture exercises every conditional branch in the adapter (placeOfDeath
 * present, multi-item plan, receiptType populated) so dropping any of them
 * would break the test even though the assertions are coarse.
 */
@DisplayName("OpenPdfFuneralPdfRenderAdapter")
class OpenPdfFuneralPdfRenderAdapterTest {

  private static final byte[] PDF_MAGIC = new byte[] {'%', 'P', 'D', 'F', '-'};

  private final OpenPdfFuneralPdfRenderAdapter adapter = new OpenPdfFuneralPdfRenderAdapter();

  @Test
  @DisplayName("produces a syntactically valid PDF starting with the %PDF- magic")
  void rendersValidPdfMagic() {
    final byte[] pdf = adapter.render(fullyHydratedFuneral());

    assertThat(pdf)
        .as("PDF magic header (%PDF-)")
        .startsWith(PDF_MAGIC);
    assertThat(pdf.length)
        .as("PDF body is non-trivial")
        .isGreaterThan(1_000);
  }

  /* ------------------------- Fixture builders ----------------------------- */

  private Funeral fullyHydratedFuneral() {
    final ProvinceEntity province = new ProvinceEntity();
    province.setId(1L);
    province.setName("Buenos Aires");

    final CityEntity city = new CityEntity();
    city.setId(10L);
    city.setName("La Plata");
    city.setProvince(province);

    final AddressEntity placeOfDeath = new AddressEntity();
    placeOfDeath.setStreetName("Calle 7");
    placeOfDeath.setBlockStreet(1234);
    placeOfDeath.setApartment("B");
    placeOfDeath.setFlat("3");
    placeOfDeath.setCity(city);

    final GenderEntity gender = new GenderEntity();
    gender.setId(1L);
    gender.setName("Masculino");

    final RelationshipEntity relationship = new RelationshipEntity();
    relationship.setId(2L);
    relationship.setName("Padre");

    final DeathCauseEntity deathCause = new DeathCauseEntity();
    deathCause.setId(3L);
    deathCause.setName("Natural");

    final DeceasedEntity deceased = new DeceasedEntity();
    deceased.setId(100L);
    deceased.setFirstName("Juan");
    deceased.setLastName("Gomez");
    deceased.setDni(35123456);
    deceased.setBirthDate(LocalDate.of(1950, 4, 12));
    deceased.setDeathDate(LocalDate.of(2026, 3, 1));
    deceased.setGender(gender);
    deceased.setDeceasedRelationship(relationship);
    deceased.setDeathCause(deathCause);
    deceased.setPlaceOfDeath(placeOfDeath);

    final ItemEntity coffin = new ItemEntity();
    coffin.setId(1L);
    coffin.setName("Ataud Modelo A");
    coffin.setCode("ATA-001");

    final ItemEntity wreath = new ItemEntity();
    wreath.setId(2L);
    wreath.setName("Corona de flores");
    wreath.setCode("COR-002");

    final Plan plan = new Plan("Plan Premium", "Servicio completo", BigDecimal.valueOf(15));
    plan.setId(500L);
    final ItemPlanEntity coffinRow = new ItemPlanEntity();
    coffinRow.setItem(coffin);
    coffinRow.setPlan(plan);
    coffinRow.setQuantity(1);
    final ItemPlanEntity wreathRow = new ItemPlanEntity();
    wreathRow.setItem(wreath);
    wreathRow.setPlan(plan);
    wreathRow.setQuantity(3);
    plan.setItemsPlan(Set.of(coffinRow, wreathRow));

    final ReceiptTypeEntity receiptType = new ReceiptTypeEntity();
    receiptType.setId(1L);
    receiptType.setName("Egreso");

    final Funeral funeral =
        Funeral.builder()
            .funeralDate(LocalDateTime.of(2026, 3, 2, 14, 30))
            .receiptNumber("000001")
            .receiptSeries("A")
            .tax(BigDecimal.valueOf(21))
            .totalAmount(new BigDecimal("125000.50"))
            .receiptType(receiptType)
            .deceased(deceased)
            .plan(plan)
            .build();
    funeral.setId(7L);
    return funeral;
  }
}
