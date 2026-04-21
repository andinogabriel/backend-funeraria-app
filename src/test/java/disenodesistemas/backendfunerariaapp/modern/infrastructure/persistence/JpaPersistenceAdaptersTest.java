package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaAffiliatePersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaBrandPersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaCategoryPersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaCityPersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaDeathCausePersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaDeceasedPersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaFuneralPersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaGenderPersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaIncomePersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaItemPersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaPlanItemPersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaPlanPersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaProvincePersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaReceiptTypePersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaRelationshipPersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaRolePersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaSupplierPersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.JpaUserPersistenceAdapter;
import disenodesistemas.backendfunerariaapp.modern.support.DomainTestDataFactory;
import disenodesistemas.backendfunerariaapp.modern.support.TestValues;
import disenodesistemas.backendfunerariaapp.persistence.repository.AffiliateRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.BrandRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.CategoryRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.CityRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.DeathCauseRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.DeceasedRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.FuneralRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.GenderRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.IncomeRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.ItemRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.ItemsPlanRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.PlanRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.ProvinceRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.ReceiptTypeRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.RelationshipRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.RoleRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.SupplierRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DisplayName("JPA Persistence Adapters")
class JpaPersistenceAdaptersTest {

  @Test
  @DisplayName(
      "Given affiliate repository responses when the affiliate persistence adapter is invoked then it delegates every query and command to the repository")
  void givenAffiliateRepositoryResponsesWhenTheAffiliatePersistenceAdapterIsInvokedThenItDelegatesEveryQueryAndCommandToTheRepository() {
    final AffiliateRepository repository = mock(AffiliateRepository.class);
    final JpaAffiliatePersistenceAdapter adapter = new JpaAffiliatePersistenceAdapter(repository);
    final AffiliateEntity affiliate = DomainTestDataFactory.affiliateEntity();

    when(repository.findByDni(30111222)).thenReturn(Optional.of(affiliate));
    when(repository.existsAffiliateEntitiesByDni(30111222)).thenReturn(Boolean.TRUE);
    when(repository.findByUserEmailOrderByStartDateDesc(TestValues.USER_EMAIL))
        .thenReturn(List.of(affiliate));
    when(repository.findAllByOrderByStartDateDesc()).thenReturn(List.of(affiliate));
    when(repository.findAllByDeceasedFalseOrderByStartDateDesc()).thenReturn(List.of(affiliate));
    when(repository.searchByFirstNameOrLastNameOrDni("juan")).thenReturn(List.of(affiliate));
    when(repository.save(affiliate)).thenReturn(affiliate);

    assertThat(adapter.findByDni(30111222)).contains(affiliate);
    assertThat(adapter.existsByDni(30111222)).isTrue();
    assertThat(adapter.findByUserEmailOrderByStartDateDesc(TestValues.USER_EMAIL))
        .containsExactly(affiliate);
    assertThat(adapter.findAllByOrderByStartDateDesc()).containsExactly(affiliate);
    assertThat(adapter.findAllByDeceasedFalseOrderByStartDateDesc()).containsExactly(affiliate);
    assertThat(adapter.searchByFirstNameOrLastNameOrDni("juan")).containsExactly(affiliate);
    assertThat(adapter.save(affiliate)).isEqualTo(affiliate);
    adapter.delete(affiliate);

    verify(repository).delete(affiliate);
  }

  @Test
  @DisplayName(
      "Given brand repository responses when the brand persistence adapter is invoked then it delegates reads and writes to the repository")
  void givenBrandRepositoryResponsesWhenTheBrandPersistenceAdapterIsInvokedThenItDelegatesReadsAndWritesToTheRepository() {
    final BrandRepository repository = mock(BrandRepository.class);
    final JpaBrandPersistenceAdapter adapter = new JpaBrandPersistenceAdapter(repository);
    final BrandEntity brand = DomainTestDataFactory.brandEntity();

    when(repository.findById(1L)).thenReturn(Optional.of(brand));
    when(repository.findAllByOrderByName()).thenReturn(List.of(brand));
    when(repository.save(brand)).thenReturn(brand);

    assertThat(adapter.findById(1L)).contains(brand);
    assertThat(adapter.findAllByOrderByName()).containsExactly(brand);
    assertThat(adapter.save(brand)).isEqualTo(brand);
    adapter.delete(brand);

    verify(repository).delete(brand);
  }

  @Test
  @DisplayName(
      "Given category repository responses when the category persistence adapter is invoked then it delegates reads and writes to the repository")
  void givenCategoryRepositoryResponsesWhenTheCategoryPersistenceAdapterIsInvokedThenItDelegatesReadsAndWritesToTheRepository() {
    final CategoryRepository repository = mock(CategoryRepository.class);
    final JpaCategoryPersistenceAdapter adapter = new JpaCategoryPersistenceAdapter(repository);
    final CategoryEntity category = DomainTestDataFactory.categoryEntity();

    when(repository.findById(1L)).thenReturn(Optional.of(category));
    when(repository.findAllByOrderByName()).thenReturn(List.of(category));
    when(repository.save(category)).thenReturn(category);

    assertThat(adapter.findById(1L)).contains(category);
    assertThat(adapter.findAllByOrderByName()).containsExactly(category);
    assertThat(adapter.save(category)).isEqualTo(category);
    adapter.delete(category);

    verify(repository).delete(category);
  }

  @Test
  @DisplayName(
      "Given city repository responses when the city persistence adapter is invoked then it delegates province-based queries to the repository")
  void givenCityRepositoryResponsesWhenTheCityPersistenceAdapterIsInvokedThenItDelegatesProvinceBasedQueriesToTheRepository() {
    final CityRepository repository = mock(CityRepository.class);
    final JpaCityPersistenceAdapter adapter = new JpaCityPersistenceAdapter(repository);
    final CityEntity city = DomainTestDataFactory.cityEntity();

    when(repository.findById(1L)).thenReturn(Optional.of(city));
    when(repository.findByProvinceIdOrderByName(1L)).thenReturn(List.of(city));

    assertThat(adapter.findById(1L)).contains(city);
    assertThat(adapter.findByProvinceIdOrderByName(1L)).containsExactly(city);
  }

  @Test
  @DisplayName(
      "Given death cause repository responses when the death cause persistence adapter is invoked then it delegates reads and writes to the repository")
  void givenDeathCauseRepositoryResponsesWhenTheDeathCausePersistenceAdapterIsInvokedThenItDelegatesReadsAndWritesToTheRepository() {
    final DeathCauseRepository repository = mock(DeathCauseRepository.class);
    final JpaDeathCausePersistenceAdapter adapter = new JpaDeathCausePersistenceAdapter(repository);
    final DeathCauseEntity deathCause = DomainTestDataFactory.deathCauseEntity();

    when(repository.findById(1L)).thenReturn(Optional.of(deathCause));
    when(repository.findAllByOrderByNameAsc()).thenReturn(List.of(deathCause));
    when(repository.save(deathCause)).thenReturn(deathCause);

    assertThat(adapter.findById(1L)).contains(deathCause);
    assertThat(adapter.findAllByOrderByNameAsc()).containsExactly(deathCause);
    assertThat(adapter.save(deathCause)).isEqualTo(deathCause);
    adapter.delete(deathCause);

    verify(repository).delete(deathCause);
  }

  @Test
  @DisplayName(
      "Given deceased repository responses when the deceased persistence adapter is invoked then it delegates reads, existence checks and writes to the repository")
  void givenDeceasedRepositoryResponsesWhenTheDeceasedPersistenceAdapterIsInvokedThenItDelegatesReadsExistenceChecksAndWritesToTheRepository() {
    final DeceasedRepository repository = mock(DeceasedRepository.class);
    final JpaDeceasedPersistenceAdapter adapter = new JpaDeceasedPersistenceAdapter(repository);
    final DeceasedEntity deceased = DomainTestDataFactory.deceasedEntity();

    when(repository.findByDni(30111222)).thenReturn(Optional.of(deceased));
    when(repository.findAllByOrderByRegisterDateDesc()).thenReturn(List.of(deceased));
    when(repository.existsByDni(30111222)).thenReturn(true);
    when(repository.save(deceased)).thenReturn(deceased);

    assertThat(adapter.findByDni(30111222)).contains(deceased);
    assertThat(adapter.findAllByOrderByRegisterDateDesc()).containsExactly(deceased);
    assertThat(adapter.existsByDni(30111222)).isTrue();
    assertThat(adapter.save(deceased)).isEqualTo(deceased);
    adapter.delete(deceased);

    verify(repository).delete(deceased);
  }

  @Test
  @DisplayName(
      "Given funeral repository responses when the funeral persistence adapter is invoked then it delegates reads, uniqueness checks and writes to the repository")
  void givenFuneralRepositoryResponsesWhenTheFuneralPersistenceAdapterIsInvokedThenItDelegatesReadsUniquenessChecksAndWritesToTheRepository() {
    final FuneralRepository repository = mock(FuneralRepository.class);
    final JpaFuneralPersistenceAdapter adapter = new JpaFuneralPersistenceAdapter(repository);
    final Funeral funeral = DomainTestDataFactory.funeral();

    when(repository.findById(1L)).thenReturn(Optional.of(funeral));
    when(repository.findAllByOrderByRegisterDateDesc()).thenReturn(List.of(funeral));
    when(repository.findFuneralsByUserEmail(TestValues.USER_EMAIL)).thenReturn(List.of(funeral));
    when(repository.existsByReceiptNumber(TestValues.FUNERAL_RECEIPT_NUMBER)).thenReturn(true);
    when(repository.save(funeral)).thenReturn(funeral);

    assertThat(adapter.findById(1L)).contains(funeral);
    assertThat(adapter.findAllByOrderByRegisterDateDesc()).containsExactly(funeral);
    assertThat(adapter.findFuneralsByUserEmail(TestValues.USER_EMAIL)).containsExactly(funeral);
    assertThat(adapter.existsByReceiptNumber(TestValues.FUNERAL_RECEIPT_NUMBER)).isTrue();
    assertThat(adapter.save(funeral)).isEqualTo(funeral);
    adapter.delete(funeral);

    verify(repository).delete(funeral);
  }

  @Test
  @DisplayName(
      "Given gender repository responses when the gender persistence adapter is invoked then it delegates reads to the repository")
  void givenGenderRepositoryResponsesWhenTheGenderPersistenceAdapterIsInvokedThenItDelegatesReadsToTheRepository() {
    final GenderRepository repository = mock(GenderRepository.class);
    final JpaGenderPersistenceAdapter adapter = new JpaGenderPersistenceAdapter(repository);
    final GenderEntity gender = DomainTestDataFactory.genderEntity();

    when(repository.findById(1L)).thenReturn(Optional.of(gender));
    when(repository.findAllByOrderByName()).thenReturn(List.of(gender));

    assertThat(adapter.findById(1L)).contains(gender);
    assertThat(adapter.findAllByOrderByName()).containsExactly(gender);
  }

  @Test
  @DisplayName(
      "Given income repository responses when the income persistence adapter is invoked then it delegates paginated queries and writes to the repository")
  void givenIncomeRepositoryResponsesWhenTheIncomePersistenceAdapterIsInvokedThenItDelegatesPaginatedQueriesAndWritesToTheRepository() {
    final IncomeRepository repository = mock(IncomeRepository.class);
    final JpaIncomePersistenceAdapter adapter = new JpaIncomePersistenceAdapter(repository);
    final IncomeEntity income = DomainTestDataFactory.incomeEntity();
    final Pageable pageable = PageRequest.of(0, 20);
    final PageImpl<IncomeEntity> page = new PageImpl<>(List.of(income));

    when(repository.findByReceiptNumber(7002L)).thenReturn(Optional.of(income));
    when(repository.findAllByDeletedFalseOrderByIdDesc()).thenReturn(List.of(income));
    when(repository.findAllByDeleted(false, pageable)).thenReturn(page);
    when(repository.save(income)).thenReturn(income);

    assertThat(adapter.findByReceiptNumber(7002L)).contains(income);
    assertThat(adapter.findAllByDeletedFalseOrderByIdDesc()).containsExactly(income);
    assertThat(adapter.findAllByDeleted(false, pageable)).isEqualTo(page);
    assertThat(adapter.save(income)).isEqualTo(income);
  }

  @Test
  @DisplayName(
      "Given item repository responses when the item persistence adapter is invoked then it delegates catalog queries and writes to the repository")
  void givenItemRepositoryResponsesWhenTheItemPersistenceAdapterIsInvokedThenItDelegatesCatalogQueriesAndWritesToTheRepository() {
    final ItemRepository repository = mock(ItemRepository.class);
    final JpaItemPersistenceAdapter adapter = new JpaItemPersistenceAdapter(repository);
    final ItemEntity item = DomainTestDataFactory.itemEntity();
    final CategoryEntity category = DomainTestDataFactory.categoryEntity();

    when(repository.findByCode(TestValues.ITEM_CODE)).thenReturn(Optional.of(item));
    when(repository.findAllByCodeIn(List.of(TestValues.ITEM_CODE))).thenReturn(List.of(item));
    when(repository.findAll()).thenReturn(List.of(item));
    when(repository.findByCategoryOrderByName(category)).thenReturn(List.of(item));
    when(repository.save(item)).thenReturn(item);
    when(repository.saveAll(List.of(item))).thenReturn(List.of(item));

    assertThat(adapter.findByCode(TestValues.ITEM_CODE)).contains(item);
    assertThat(adapter.findAllByCodeIn(List.of(TestValues.ITEM_CODE))).containsExactly(item);
    assertThat(adapter.findAll()).containsExactly(item);
    assertThat(adapter.findByCategoryOrderByName(category)).containsExactly(item);
    assertThat(adapter.save(item)).isEqualTo(item);
    assertThat(adapter.saveAll(List.of(item))).containsExactly(item);
    adapter.delete(item);

    verify(repository).delete(item);
  }

  @Test
  @DisplayName(
      "Given item-plan repository responses when the plan-item persistence adapter is invoked then it delegates batch persistence to the repository")
  void givenItemPlanRepositoryResponsesWhenThePlanItemPersistenceAdapterIsInvokedThenItDelegatesBatchPersistenceToTheRepository() {
    final ItemsPlanRepository repository = mock(ItemsPlanRepository.class);
    final JpaPlanItemPersistenceAdapter adapter = new JpaPlanItemPersistenceAdapter(repository);
    final ItemPlanEntity itemPlan = DomainTestDataFactory.itemPlanEntity();

    when(repository.saveAll(List.of(itemPlan))).thenReturn(List.of(itemPlan));

    assertThat(adapter.saveAll(List.of(itemPlan))).containsExactly(itemPlan);
  }

  @Test
  @DisplayName(
      "Given plan repository responses when the plan persistence adapter is invoked then it delegates reads and writes to the repository")
  void givenPlanRepositoryResponsesWhenThePlanPersistenceAdapterIsInvokedThenItDelegatesReadsAndWritesToTheRepository() {
    final PlanRepository repository = mock(PlanRepository.class);
    final JpaPlanPersistenceAdapter adapter = new JpaPlanPersistenceAdapter(repository);
    final Plan plan = DomainTestDataFactory.plan();
    final ItemEntity item = DomainTestDataFactory.itemEntity();

    when(repository.findById(1L)).thenReturn(Optional.of(plan));
    when(repository.findAllByOrderByIdDesc()).thenReturn(List.of(plan));
    when(repository.findPlansContainingAnyOfThisItems(List.of(item))).thenReturn(List.of(plan));
    when(repository.save(plan)).thenReturn(plan);
    when(repository.saveAll(List.of(plan))).thenReturn(List.of(plan));

    assertThat(adapter.findById(1L)).contains(plan);
    assertThat(adapter.findAllByOrderByIdDesc()).containsExactly(plan);
    assertThat(adapter.findPlansContainingAnyOfThisItems(List.of(item))).containsExactly(plan);
    assertThat(adapter.save(plan)).isEqualTo(plan);
    assertThat(adapter.saveAll(List.of(plan))).containsExactly(plan);
    adapter.delete(plan);

    verify(repository).delete(plan);
  }

  @Test
  @DisplayName(
      "Given province repository responses when the province persistence adapter is invoked then it delegates reads to the repository")
  void givenProvinceRepositoryResponsesWhenTheProvincePersistenceAdapterIsInvokedThenItDelegatesReadsToTheRepository() {
    final ProvinceRepository repository = mock(ProvinceRepository.class);
    final JpaProvincePersistenceAdapter adapter = new JpaProvincePersistenceAdapter(repository);
    final ProvinceEntity province = DomainTestDataFactory.provinceEntity();

    when(repository.findById(1L)).thenReturn(Optional.of(province));
    when(repository.findAllByOrderByName()).thenReturn(List.of(province));

    assertThat(adapter.findById(1L)).contains(province);
    assertThat(adapter.findAllByOrderByName()).containsExactly(province);
  }

  @Test
  @DisplayName(
      "Given receipt type repository responses when the receipt type persistence adapter is invoked then it delegates reads to the repository")
  void givenReceiptTypeRepositoryResponsesWhenTheReceiptTypePersistenceAdapterIsInvokedThenItDelegatesReadsToTheRepository() {
    final ReceiptTypeRepository repository = mock(ReceiptTypeRepository.class);
    final JpaReceiptTypePersistenceAdapter adapter =
        new JpaReceiptTypePersistenceAdapter(repository);
    final ReceiptTypeEntity receiptType = DomainTestDataFactory.receiptTypeEntity();

    when(repository.findAllByOrderByName()).thenReturn(List.of(receiptType));
    when(repository.findByNameIsContainingIgnoreCase("Factura A"))
        .thenReturn(Optional.of(receiptType));

    assertThat(adapter.findAllByOrderByName()).containsExactly(receiptType);
    assertThat(adapter.findByNameIsContainingIgnoreCase("Factura A")).contains(receiptType);
  }

  @Test
  @DisplayName(
      "Given relationship repository responses when the relationship persistence adapter is invoked then it delegates reads to the repository")
  void givenRelationshipRepositoryResponsesWhenTheRelationshipPersistenceAdapterIsInvokedThenItDelegatesReadsToTheRepository() {
    final RelationshipRepository repository = mock(RelationshipRepository.class);
    final JpaRelationshipPersistenceAdapter adapter =
        new JpaRelationshipPersistenceAdapter(repository);
    final RelationshipEntity relationship = DomainTestDataFactory.relationshipEntity();

    when(repository.findById(1L)).thenReturn(Optional.of(relationship));
    when(repository.findAllByOrderByName()).thenReturn(List.of(relationship));

    assertThat(adapter.findById(1L)).contains(relationship);
    assertThat(adapter.findAllByOrderByName()).containsExactly(relationship);
  }

  @Test
  @DisplayName(
      "Given role repository responses when the role persistence adapter is invoked then it delegates reads to the repository")
  void givenRoleRepositoryResponsesWhenTheRolePersistenceAdapterIsInvokedThenItDelegatesReadsToTheRepository() {
    final RoleRepository repository = mock(RoleRepository.class);
    final JpaRolePersistenceAdapter adapter = new JpaRolePersistenceAdapter(repository);
    final RoleEntity roleEntity = DomainTestDataFactory.roleEntity(Role.ROLE_USER, 1L);

    when(repository.findById(1L)).thenReturn(Optional.of(roleEntity));
    when(repository.findByName(Role.ROLE_USER)).thenReturn(Optional.of(roleEntity));
    when(repository.findAll()).thenReturn(List.of(roleEntity));

    assertThat(adapter.findById(1L)).contains(roleEntity);
    assertThat(adapter.findByName(Role.ROLE_USER)).contains(roleEntity);
    assertThat(adapter.findAll()).containsExactly(roleEntity);
  }

  @Test
  @DisplayName(
      "Given supplier repository responses when the supplier persistence adapter is invoked then it delegates reads and writes to the repository")
  void givenSupplierRepositoryResponsesWhenTheSupplierPersistenceAdapterIsInvokedThenItDelegatesReadsAndWritesToTheRepository() {
    final SupplierRepository repository = mock(SupplierRepository.class);
    final JpaSupplierPersistenceAdapter adapter = new JpaSupplierPersistenceAdapter(repository);
    final SupplierEntity supplier = DomainTestDataFactory.supplierEntity();

    when(repository.findByNif(TestValues.SUPPLIER_NIF)).thenReturn(Optional.of(supplier));
    when(repository.findAllByOrderByIdDesc()).thenReturn(List.of(supplier));
    when(repository.save(supplier)).thenReturn(supplier);

    assertThat(adapter.findByNif(TestValues.SUPPLIER_NIF)).contains(supplier);
    assertThat(adapter.findAllByOrderByIdDesc()).containsExactly(supplier);
    assertThat(adapter.save(supplier)).isEqualTo(supplier);
    adapter.delete(supplier);

    verify(repository).delete(supplier);
  }

  @Test
  @DisplayName(
      "Given user repository responses when the user persistence adapter is invoked then it delegates reads, paging and writes to the repository")
  void givenUserRepositoryResponsesWhenTheUserPersistenceAdapterIsInvokedThenItDelegatesReadsPagingAndWritesToTheRepository() {
    final UserRepository repository = mock(UserRepository.class);
    final JpaUserPersistenceAdapter adapter = new JpaUserPersistenceAdapter(repository);
    final UserEntity user = DomainTestDataFactory.userEntity();
    final Pageable pageable = PageRequest.of(0, 10);
    final PageImpl<UserEntity> page = new PageImpl<>(List.of(user));

    when(repository.findById(1L)).thenReturn(Optional.of(user));
    when(repository.findByEmail(TestValues.USER_EMAIL)).thenReturn(Optional.of(user));
    when(repository.findAll(pageable)).thenReturn(page);
    when(repository.findAllByOrderByStartDateDesc()).thenReturn(List.of(user));
    when(repository.save(user)).thenReturn(user);

    assertThat(adapter.findById(1L)).contains(user);
    assertThat(adapter.findByEmail(TestValues.USER_EMAIL)).contains(user);
    assertThat(adapter.findAll(pageable)).isEqualTo(page);
    assertThat(adapter.findAllByOrderByStartDateDesc()).containsExactly(user);
    assertThat(adapter.save(user)).isEqualTo(user);
  }
}
