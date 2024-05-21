package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.utils.IncomeTestDataFactory.getIncome;
import static disenodesistemas.backendfunerariaapp.utils.IncomeTestDataFactory.getIncomeRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.IncomeResponseDto;
import disenodesistemas.backendfunerariaapp.entities.IncomeEntity;
import disenodesistemas.backendfunerariaapp.service.IncomeService;
import java.util.List;
import javax.persistence.EntityManager;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class IncomeControllerTest
    extends AbstractControllerTest<IncomeRequestDto, IncomeResponseDto, IncomeEntity, Long> {

  @Mock private IncomeService incomeService;
  @Mock private EntityManager entityManager;
  @Mock private SecurityContext securityContext;
  @Mock private Authentication authentication;
  @Mock private Session session;
  @Mock private Filter filter;

  @InjectMocks private IncomeController sut;

  private static final Long EXISTING_INCOME_IDENTIFIER = 1L;

  @Test
  void findAll() {
    testFindAll(
        () -> List.of(responseDto),
        sut::findAll,
        () -> List.of(responseDto),
        incomeService::findAll);
    then(incomeService).should(times(1)).findAll();
  }

  @Test
  void findById() {
    testFindByID(
        incomeService::findByReceiptNumber, sut::findById, EXISTING_INCOME_IDENTIFIER, responseDto);
    then(incomeService).should(times(1)).findByReceiptNumber(EXISTING_INCOME_IDENTIFIER);
  }

  @Test
  void getIncomesPaginated() {
    final int page = 0;
    final int limit = 5;
    final String sortBy = "incomeDate";
    final String sortDir = "desc";
    final Page<IncomeResponseDto> expectedResult = new PageImpl<>(List.of(responseDto));

    given(entityManager.unwrap(Session.class)).willReturn(session);
    given(session.enableFilter("deletedIncomesFilter")).willReturn(filter);
    given(incomeService.getIncomesPaginated(page, limit, sortBy, sortDir))
        .willReturn(expectedResult);

    final Page<IncomeResponseDto> actualResponse =
        sut.getIncomesPaginated(Boolean.FALSE, page, limit, sortBy, sortDir);

    assertEquals(expectedResult, actualResponse);
    then(session).should(times(1)).enableFilter("deletedIncomesFilter");
    then(filter).should(times(1)).setParameter("isDeleted", Boolean.FALSE);
    then(session).should(times(1)).disableFilter("deletedIncomesFilter");
    then(incomeService).should(times(1)).getIncomesPaginated(page, limit, sortBy, sortDir);
  }

  @Test
  void create() {
    given(securityContext.getAuthentication()).willReturn(authentication);
    given(authentication.getName()).willReturn("email_test@gmail.com");
    SecurityContextHolder.setContext(securityContext);
    testCreate(incomeService::create, sut::create, requestDto, responseDto);
    then(incomeService).should(times(1)).create(requestDto);
  }

  @Test
  void update() {
    given(securityContext.getAuthentication()).willReturn(authentication);
    given(authentication.getName()).willReturn("email_test@gmail.com");
    SecurityContextHolder.setContext(securityContext);
    testUpdate(
        incomeService::update, sut::update, EXISTING_INCOME_IDENTIFIER, requestDto, responseDto);
    then(incomeService).should(times(1)).update(EXISTING_INCOME_IDENTIFIER, requestDto);
  }

  @Test
  void delete() {
    testDelete(sut::delete, EXISTING_INCOME_IDENTIFIER, "DELETE INCOME");
    then(incomeService).should(times(1)).delete(EXISTING_INCOME_IDENTIFIER);
  }

  @Override
  protected IncomeRequestDto getRequestDto() {
    return getIncomeRequest();
  }

  @Override
  protected Class<IncomeResponseDto> getResponseDtoClass() {
    return IncomeResponseDto.class;
  }

  @Override
  protected IncomeEntity getEntity() {
    return getIncome();
  }
}
