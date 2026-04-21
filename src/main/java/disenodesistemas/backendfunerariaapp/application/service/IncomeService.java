package disenodesistemas.backendfunerariaapp.application.service;

import disenodesistemas.backendfunerariaapp.web.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.IncomeResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;

public interface IncomeService {

  IncomeResponseDto create(IncomeRequestDto dto);

  IncomeResponseDto update(Long receiptNumber, IncomeRequestDto dto);

  void delete(Long receiptNumber);

  List<IncomeResponseDto> findAll();

  IncomeResponseDto findById(Long receiptNumber);

  Page<IncomeResponseDto> getIncomesPaginated(
      boolean isDeleted, int page, int limit, String sortBy, String sortDir);

  IncomeResponseDto findByReceiptNumber(Long receiptNumber);
}
