package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.IncomeResponseDto;
import org.springframework.data.domain.Page;

public interface IncomeService extends CommonService<IncomeResponseDto, IncomeRequestDto, Long> {
  Page<IncomeResponseDto> getIncomesPaginated(int page, int limit, String sortBy, String sortDir);

  IncomeResponseDto findByReceiptNumber(Long receiptNumber);
}
