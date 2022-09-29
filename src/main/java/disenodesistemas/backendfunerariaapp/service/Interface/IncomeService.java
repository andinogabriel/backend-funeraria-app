package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.IncomeResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IncomeService {

    IncomeResponseDto createIncome(IncomeRequestDto incomeRequestDto);

    List<IncomeResponseDto> getAllIncomes(final Boolean deleted);

    IncomeResponseDto updateIncome(Long receiptNumber, IncomeRequestDto incomeRequestDto);

    void deleteIncome(Long receiptNumber);

    Page<IncomeResponseDto> getIncomesPaginated(int page, int limit, String sortBy, String sortDir);

    IncomeResponseDto findByReceiptNumber(Long receiptNumber);



}
