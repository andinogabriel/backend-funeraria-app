package disenodesistemas.backendfunerariaapp.modern.application.usecase.additional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.CategoryPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.DeceasedPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.GenderPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.IncomePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.ItemPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.PlanPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.UserPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.affiliate.AffiliateQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.category.CategoryQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.deceased.DeceasedCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.deceased.DeceasedQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.gender.GenderQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.income.IncomeQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.item.ItemQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.GenderEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.AffiliateMapper;
import disenodesistemas.backendfunerariaapp.mapping.DeceasedMapper;
import disenodesistemas.backendfunerariaapp.mapping.GenderMapper;
import disenodesistemas.backendfunerariaapp.mapping.IncomeMapper;
import disenodesistemas.backendfunerariaapp.mapping.ItemMapper;
import disenodesistemas.backendfunerariaapp.mapping.PlanMapper;
import disenodesistemas.backendfunerariaapp.mapping.UserMapper;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.web.dto.response.AffiliateResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeceasedResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.GenderResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.IncomeResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ItemResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.PlanResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.UserResponseDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@DisplayName("Remaining Use Cases Coverage")
class RemainingUseCasesCoverageTest {

  @Test
  @DisplayName(
      "Given persisted incomes when income queries are executed then the use case maps the list, resolves individual receipts and builds a zero-based page")
  void givenPersistedIncomesWhenIncomeQueriesAreExecutedThenTheUseCaseMapsTheListResolvesIndividualReceiptsAndBuildsAZeroBasedPage() {
    final IncomePersistencePort incomePersistencePort = mock(IncomePersistencePort.class);
    final IncomeMapper incomeMapper = mock(IncomeMapper.class);
    final IncomeQueryUseCase incomeQueryUseCase = new IncomeQueryUseCase(incomePersistencePort, incomeMapper);
    final IncomeEntity income = new IncomeEntity();
    final IncomeResponseDto response =
        new IncomeResponseDto("7002", "1001", null, null, null, null, null, null, null, null, null);

    when(incomePersistencePort.findAllByDeletedFalseOrderByIdDesc()).thenReturn(List.of(income));
    when(incomePersistencePort.findByReceiptNumber(7002L)).thenReturn(Optional.of(income));
    when(incomePersistencePort.findAllByDeleted(org.mockito.Mockito.eq(false), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(income)));
    when(incomeMapper.toDto(income)).thenReturn(response);

    assertThat(incomeQueryUseCase.findAll()).containsExactly(response);
    assertThat(incomeQueryUseCase.findById(7002L)).isEqualTo(response);
    assertThat(incomeQueryUseCase.findByReceiptNumber(7002L)).isEqualTo(response);
    assertThat(incomeQueryUseCase.getIncomesPaginated(false, 1, 10, "receiptNumber", "desc").getContent())
        .containsExactly(response);
  }

  @Test
  @DisplayName(
      "Given persisted users when user queries are executed then the use case resolves the entities and maps the ordered response list")
  void givenPersistedUsersWhenUserQueriesAreExecutedThenTheUseCaseResolvesTheEntitiesAndMapsTheOrderedResponseList() {
    final UserPersistencePort userPersistencePort = mock(UserPersistencePort.class);
    final UserMapper userMapper = mock(UserMapper.class);
    final UserQueryUseCase userQueryUseCase = new UserQueryUseCase(userPersistencePort, userMapper);
    final UserEntity user = SecurityTestDataFactory.userEntity();
    final UserResponseDto response =
        new UserResponseDto(1L, "John", "Doe", "john.doe@example.com", true, null, true, List.of(), List.of(), user.getRoles().stream().map(role -> new disenodesistemas.backendfunerariaapp.web.dto.RolesDto(role.getId(), role.getName().name())).collect(java.util.stream.Collectors.toSet()));

    when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));
    when(userPersistencePort.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
    when(userPersistencePort.findAllByOrderByStartDateDesc()).thenReturn(List.of(user));
    when(userMapper.toDto(user)).thenReturn(response);

    assertThat(userQueryUseCase.getUserById(1L)).isEqualTo(user);
    assertThat(userQueryUseCase.getUserByEmail("john.doe@example.com")).isEqualTo(user);
    assertThat(userQueryUseCase.findAll()).containsExactly(response);
  }

  @Test
  @DisplayName(
      "Given persisted affiliates when affiliate queries are executed then the use case maps active and complete lists and resolves affiliates by dni")
  void givenPersistedAffiliatesWhenAffiliateQueriesAreExecutedThenTheUseCaseMapsActiveAndCompleteListsAndResolvesAffiliatesByDni() {
    final AffiliatePersistencePort affiliatePersistencePort = mock(AffiliatePersistencePort.class);
    final AffiliateMapper affiliateMapper = mock(AffiliateMapper.class);
    final AuthenticatedUserPort authenticatedUserPort = mock(AuthenticatedUserPort.class);
    final AffiliateQueryUseCase affiliateQueryUseCase =
        new AffiliateQueryUseCase(affiliatePersistencePort, affiliateMapper, authenticatedUserPort);
    final AffiliateEntity affiliate = AffiliateEntity.builder().dni(30111222).build();
    final AffiliateResponseDto response =
        new AffiliateResponseDto("Juan", "Perez", 30111222, LocalDate.of(1980, 1, 1), null, Boolean.FALSE, null, null, null, List.of(), List.of());

    when(affiliatePersistencePort.findAllByDeceasedFalseOrderByStartDateDesc()).thenReturn(List.of(affiliate));
    when(affiliatePersistencePort.findAllByOrderByStartDateDesc()).thenReturn(List.of(affiliate));
    when(affiliatePersistencePort.findByDni(30111222)).thenReturn(Optional.of(affiliate));
    when(affiliatePersistencePort.searchByFirstNameOrLastNameOrDni("juan")).thenReturn(List.of(affiliate));
    when(affiliateMapper.toDto(affiliate)).thenReturn(response);

    assertThat(affiliateQueryUseCase.findAllByDeceasedFalse()).containsExactly(response);
    assertThat(affiliateQueryUseCase.findAll()).containsExactly(response);
    assertThat(affiliateQueryUseCase.findById(30111222)).isEqualTo(response);
    assertThat(affiliateQueryUseCase.findAffiliatesByFirstNameOrLastNameOrDniContaining(" juan "))
        .containsExactly(response);
  }

  @Test
  @DisplayName(
      "Given persisted plans items and genders when simple query use cases are executed then they resolve the mapped responses and throw not found for missing items")
  void givenPersistedPlansItemsAndGendersWhenSimpleQueryUseCasesAreExecutedThenTheyResolveTheMappedResponsesAndThrowNotFoundForMissingItems() {
    final PlanPersistencePort planPersistencePort = mock(PlanPersistencePort.class);
    final PlanMapper planMapper = mock(PlanMapper.class);
    final PlanQueryUseCase planQueryUseCase = new PlanQueryUseCase(planPersistencePort, planMapper);
    final CategoryPersistencePort categoryPersistencePort = mock(CategoryPersistencePort.class);
    final CategoryQueryUseCase categoryQueryUseCase =
        new CategoryQueryUseCase(categoryPersistencePort, mock(disenodesistemas.backendfunerariaapp.mapping.CategoryMapper.class));
    final ItemPersistencePort itemPersistencePort = mock(ItemPersistencePort.class);
    final ItemMapper itemMapper = mock(ItemMapper.class);
    final ItemQueryUseCase itemQueryUseCase =
        new ItemQueryUseCase(itemPersistencePort, itemMapper, categoryQueryUseCase);
    final GenderPersistencePort genderPersistencePort = mock(GenderPersistencePort.class);
    final GenderMapper genderMapper = mock(GenderMapper.class);
    final GenderQueryUseCase genderQueryUseCase = new GenderQueryUseCase(genderPersistencePort, genderMapper);

    final Plan plan = new Plan();
    plan.setId(1L);
    final ItemEntity item = new ItemEntity();
    item.setCode("ITEM-001");
    final CategoryEntity category = new CategoryEntity("Urnas", "Productos");
    final GenderEntity gender = new GenderEntity("Masculino");
    final PlanResponseDto planResponse = new PlanResponseDto(1L, "Plan Basico", null, null, null, null, java.util.Set.of());
    final ItemResponseDto itemResponse =
        new ItemResponseDto(
            "Urna", null, "ITEM-001", null, null, null, null, null, null, null, null, null, null,
            null, null);
    final GenderResponseDto genderResponse = new GenderResponseDto(1L, "Masculino");

    when(planPersistencePort.findAllByOrderByIdDesc()).thenReturn(List.of(plan));
    when(planPersistencePort.findById(1L)).thenReturn(Optional.of(plan));
    when(planMapper.toDto(plan)).thenReturn(planResponse);
    when(categoryPersistencePort.findById(1L)).thenReturn(Optional.of(category));
    when(itemPersistencePort.findAll()).thenReturn(List.of(item));
    when(itemPersistencePort.findByCategoryOrderByName(category)).thenReturn(List.of(item));
    when(itemPersistencePort.findByCode("ITEM-001")).thenReturn(Optional.of(item));
    when(itemPersistencePort.findByCode("MISSING")).thenReturn(Optional.empty());
    when(itemMapper.toDto(item)).thenReturn(itemResponse);
    when(genderPersistencePort.findAllByOrderByName()).thenReturn(List.of(gender));
    when(genderMapper.toDto(gender)).thenReturn(genderResponse);

    assertThat(planQueryUseCase.findAll()).containsExactly(planResponse);
    assertThat(planQueryUseCase.findById(1L)).isEqualTo(planResponse);
    assertThat(planQueryUseCase.findEntityById(1L)).isEqualTo(plan);
    assertThat(itemQueryUseCase.findAll()).containsExactly(itemResponse);
    assertThat(itemQueryUseCase.getItemsByCategoryId(1L)).containsExactly(itemResponse);
    assertThat(itemQueryUseCase.findById("ITEM-001")).isEqualTo(itemResponse);
    assertThat(genderQueryUseCase.getGenders()).containsExactly(genderResponse);

    assertThatThrownBy(() -> itemQueryUseCase.getItemByCode("MISSING"))
        .isInstanceOf(NotFoundException.class)
        .extracting("message")
        .isEqualTo("item.error.code.not.found");
  }

  @Test
  @DisplayName(
      "Given deceased commands not covered by the primary suite when create and delete are executed then the use case persists the mapped aggregate and deletes the resolved entity")
  void givenDeceasedCommandsNotCoveredByThePrimarySuiteWhenCreateAndDeleteAreExecutedThenTheUseCasePersistsTheMappedAggregateAndDeletesTheResolvedEntity() {
    final DeceasedPersistencePort deceasedPersistencePort = mock(DeceasedPersistencePort.class);
    final DeceasedMapper deceasedMapper = mock(DeceasedMapper.class);
    final DeceasedQueryUseCase deceasedQueryUseCase =
        new DeceasedQueryUseCase(deceasedPersistencePort, deceasedMapper);
    final DeceasedCommandUseCase deceasedCommandUseCase =
        new DeceasedCommandUseCase(deceasedPersistencePort, deceasedMapper, deceasedQueryUseCase);
    final DeceasedEntity entity = new DeceasedEntity();
    entity.setDni(30111222);
    final disenodesistemas.backendfunerariaapp.web.dto.request.DeceasedRequestDto request =
        disenodesistemas.backendfunerariaapp.web.dto.request.DeceasedRequestDto.builder()
            .firstName("Juan")
            .lastName("Perez")
            .dni(30111222)
            .birthDate(LocalDate.of(1970, 1, 1))
            .deathDate(LocalDate.of(2026, 4, 16))
            .build();
    final DeceasedResponseDto response =
        new DeceasedResponseDto(1L, "Juan", "Perez", 30111222, false, LocalDate.of(1970, 1, 1), null, LocalDate.of(2026, 4, 16), null, null, null, null, null);

    when(deceasedMapper.toEntity(request)).thenReturn(entity);
    when(deceasedPersistencePort.save(entity)).thenReturn(entity);
    when(deceasedPersistencePort.findByDni(30111222)).thenReturn(Optional.of(entity));
    when(deceasedMapper.toDto(entity)).thenReturn(response);

    assertThat(deceasedCommandUseCase.create(request)).isEqualTo(response);
    deceasedCommandUseCase.delete(30111222);

    verify(deceasedPersistencePort).delete(entity);
  }
}
