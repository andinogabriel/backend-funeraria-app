package disenodesistemas.backendfunerariaapp.modern.support;

import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.BrandEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.CityEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.DeathCauseEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.domain.entity.GenderEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.domain.entity.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.RoleEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.SupplierEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.Role;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public final class DomainTestDataFactory {

  private DomainTestDataFactory() {}

  public static UserEntity userEntity() {
    final UserEntity userEntity =
        new UserEntity(
            TestValues.USER_EMAIL,
            TestValues.USER_FIRST_NAME,
            TestValues.USER_LAST_NAME,
            TestValues.ENCODED_PASSWORD);
    userEntity.setId(1L);
    userEntity.setEnabled(true);
    userEntity.setActive(Boolean.TRUE);
    userEntity.setRoles(Set.of(roleEntity(Role.ROLE_USER, 1L)));
    return userEntity;
  }

  public static RoleEntity roleEntity(final Role role, final Long id) {
    final RoleEntity roleEntity = new RoleEntity(role);
    roleEntity.setId(id);
    return roleEntity;
  }

  public static BrandEntity brandEntity() {
    final BrandEntity brandEntity =
        new BrandEntity(TestValues.BRAND_NAME, TestValues.BRAND_WEB_PAGE);
    brandEntity.setId(1L);
    return brandEntity;
  }

  public static CategoryEntity categoryEntity() {
    final CategoryEntity categoryEntity =
        new CategoryEntity(TestValues.CATEGORY_NAME, TestValues.CATEGORY_DESCRIPTION);
    categoryEntity.setId(1L);
    return categoryEntity;
  }

  public static ProvinceEntity provinceEntity() {
    return ProvinceEntity.builder().id(1L).name("Cordoba").code31662("AR-X").build();
  }

  public static CityEntity cityEntity() {
    return CityEntity.builder()
        .id(1L)
        .province(provinceEntity())
        .name("Cordoba")
        .zipCode("5000")
        .build();
  }

  public static GenderEntity genderEntity() {
    final GenderEntity genderEntity = new GenderEntity("Masculino");
    genderEntity.setId(1L);
    return genderEntity;
  }

  public static RelationshipEntity relationshipEntity() {
    final RelationshipEntity relationshipEntity = new RelationshipEntity("Padre");
    relationshipEntity.setId(1L);
    return relationshipEntity;
  }

  public static DeathCauseEntity deathCauseEntity() {
    final DeathCauseEntity deathCauseEntity = new DeathCauseEntity("Natural");
    deathCauseEntity.setId(1L);
    return deathCauseEntity;
  }

  public static ReceiptTypeEntity receiptTypeEntity() {
    final ReceiptTypeEntity receiptTypeEntity = new ReceiptTypeEntity("Factura A");
    receiptTypeEntity.setId(1L);
    return receiptTypeEntity;
  }

  public static ItemEntity itemEntity() {
    final ItemEntity itemEntity =
        ItemEntity.builder()
            .name(TestValues.ITEM_NAME)
            .description(TestValues.ITEM_DESCRIPTION)
            .code(TestValues.ITEM_CODE)
            .price(new BigDecimal("100.00"))
            .category(categoryEntity())
            .brand(brandEntity())
            .build();
    itemEntity.setId(1L);
    itemEntity.setStock(5);
    return itemEntity;
  }

  public static Plan plan() {
    final Plan plan = new Plan("Plan Oro", "Cobertura completa", new BigDecimal("25.00"));
    plan.setId(1L);
    plan.setPrice(new BigDecimal("1500.00"));
    return plan;
  }

  public static ItemPlanEntity itemPlanEntity() {
    return new ItemPlanEntity(plan(), itemEntity(), 1);
  }

  public static SupplierEntity supplierEntity() {
    final SupplierEntity supplierEntity =
        new SupplierEntity(
            TestValues.SUPPLIER_NAME,
            TestValues.SUPPLIER_NIF,
            TestValues.SUPPLIER_WEB_PAGE,
            TestValues.SUPPLIER_EMAIL);
    supplierEntity.setId(1L);
    return supplierEntity;
  }

  public static AffiliateEntity affiliateEntity() {
    return AffiliateEntity.builder()
        .id(1L)
        .firstName("Juan")
        .lastName("Perez")
        .dni(30111222)
        .birthDate(LocalDate.of(1980, 1, 1))
        .deceased(Boolean.FALSE)
        .gender(genderEntity())
        .user(userEntity())
        .relationship(relationshipEntity())
        .build();
  }

  public static DeceasedEntity deceasedEntity() {
    final DeceasedEntity deceasedEntity =
        DeceasedEntity.builder()
            .firstName("Juan")
            .lastName("Perez")
            .dni(30111222)
            .birthDate(LocalDate.of(1970, 1, 1))
            .deathDate(LocalDate.of(2026, 4, 10))
            .affiliated(false)
            .deceasedRelationship(relationshipEntity())
            .deceasedUser(userEntity())
            .gender(genderEntity())
            .deathCause(deathCauseEntity())
            .build();
    deceasedEntity.setId(1L);
    return deceasedEntity;
  }

  public static Funeral funeral() {
    final Funeral funeral =
        Funeral.builder()
            .funeralDate(LocalDateTime.of(2026, 4, 11, 10, 0))
            .receiptNumber(TestValues.FUNERAL_RECEIPT_NUMBER)
            .receiptSeries(TestValues.FUNERAL_RECEIPT_SERIES)
            .tax(new BigDecimal("21.00"))
            .totalAmount(new BigDecimal("121.00"))
            .receiptType(receiptTypeEntity())
            .deceased(deceasedEntity())
            .plan(plan())
            .build();
    funeral.setId(1L);
    return funeral;
  }

  public static IncomeEntity incomeEntity() {
    final IncomeEntity incomeEntity =
        IncomeEntity.builder()
            .receiptNumber(7002L)
            .receiptSeries(1001L)
            .tax(BigDecimal.ZERO)
            .receiptType(receiptTypeEntity())
            .incomeSupplier(supplierEntity())
            .incomeUser(userEntity())
            .build();
    incomeEntity.setId(1L);
    incomeEntity.setTotalAmount(BigDecimal.ZERO);
    return incomeEntity;
  }
}
