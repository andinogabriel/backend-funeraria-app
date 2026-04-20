package disenodesistemas.backendfunerariaapp.application.support;

import disenodesistemas.backendfunerariaapp.domain.entity.IncomeDetailEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.IncomeDetailRequestDto;
import java.math.BigDecimal;
import java.util.List;

public interface IncomeDetailService {

  List<IncomeDetailEntity> mapDetails(List<IncomeDetailRequestDto> detailDtos);

  void applyStockAndRefreshPrices(List<IncomeDetailEntity> incomeDetails);

  void restoreStock(List<IncomeDetailEntity> incomeDetails);

  BigDecimal calculateTotal(List<IncomeDetailEntity> incomeDetails, BigDecimal tax);
}
