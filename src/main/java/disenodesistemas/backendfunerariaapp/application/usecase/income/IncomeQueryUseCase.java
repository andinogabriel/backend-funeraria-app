package disenodesistemas.backendfunerariaapp.application.usecase.income;

import disenodesistemas.backendfunerariaapp.application.port.out.IncomePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.IncomeMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.IncomeResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IncomeQueryUseCase {

  private static final String ASC = "asc";

  private final IncomePersistencePort incomePersistencePort;
  private final IncomeMapper incomeMapper;

  @Transactional(readOnly = true)
  public List<IncomeResponseDto> findAll() {
    return incomePersistencePort.findAllByDeletedFalseOrderByIdDesc().stream()
        .map(incomeMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public IncomeResponseDto findById(final Long receiptNumber) {
    return incomeMapper.toDto(findEntityByReceiptNumber(receiptNumber));
  }

  @Transactional(readOnly = true)
  public IncomeResponseDto findByReceiptNumber(final Long receiptNumber) {
    return incomeMapper.toDto(findEntityByReceiptNumber(receiptNumber));
  }

  @Transactional(readOnly = true)
  public Page<IncomeResponseDto> getIncomesPaginated(
      final boolean isDeleted,
      int page,
      final int limit,
      final String sortBy,
      final String sortDir) {
    page = page > 0 ? page - 1 : page;
    final Pageable pageable =
        PageRequest.of(
            page,
            limit,
            Strings.CI.equals(sortDir, ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending());

    final Page<IncomeEntity> entities = incomePersistencePort.findAllByDeleted(isDeleted, pageable);
    return new PageImpl<>(
        entities.getContent().stream().map(incomeMapper::toDto).toList(),
        pageable,
        entities.getTotalElements());
  }

  @Transactional(readOnly = true)
  public IncomeEntity findEntityByReceiptNumber(final Long receiptNumber) {
    return incomePersistencePort
        .findByReceiptNumber(receiptNumber)
        .orElseThrow(() -> new NotFoundException("income.error.not.found"));
  }
}
