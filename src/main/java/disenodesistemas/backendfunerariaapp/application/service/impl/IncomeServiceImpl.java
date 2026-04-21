package disenodesistemas.backendfunerariaapp.application.service.impl;

import disenodesistemas.backendfunerariaapp.application.service.IncomeService;
import disenodesistemas.backendfunerariaapp.application.usecase.income.IncomeCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.income.IncomeQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.IncomeResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncomeServiceImpl implements IncomeService {

  private final IncomeCommandUseCase incomeCommandUseCase;
  private final IncomeQueryUseCase incomeQueryUseCase;

  @Override
  public IncomeResponseDto create(final IncomeRequestDto incomeRequestDto) {
    return incomeCommandUseCase.create(incomeRequestDto);
  }

  @Override
  public List<IncomeResponseDto> findAll() {
    return incomeQueryUseCase.findAll();
  }

  @Override
  public IncomeResponseDto findById(final Long receiptNumber) {
    return incomeQueryUseCase.findById(receiptNumber);
  }

  @Override
  public IncomeResponseDto update(final Long receiptNumber, final IncomeRequestDto incomeRequestDto) {
    return incomeCommandUseCase.update(receiptNumber, incomeRequestDto);
  }

  @Override
  public void delete(final Long receiptNumber) {
    incomeCommandUseCase.delete(receiptNumber);
  }

  @Override
  public Page<IncomeResponseDto> getIncomesPaginated(
      final boolean isDeleted,
      int page,
      final int limit,
      final String sortBy,
      final String sortDir) {
    return incomeQueryUseCase.getIncomesPaginated(isDeleted, page, limit, sortBy, sortDir);
  }

  @Override
  public IncomeResponseDto findByReceiptNumber(final Long receiptNumber) {
    return incomeQueryUseCase.findByReceiptNumber(receiptNumber);
  }
}

