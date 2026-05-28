package disenodesistemas.backendfunerariaapp.application.usecase.income;

import disenodesistemas.backendfunerariaapp.application.port.out.IncomePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.ReceiptNumberGeneratorPort;
import disenodesistemas.backendfunerariaapp.application.support.IncomeDetailService;
import disenodesistemas.backendfunerariaapp.application.usecase.supplier.SupplierQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeDetailEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import disenodesistemas.backendfunerariaapp.mapping.IncomeMapper;
import disenodesistemas.backendfunerariaapp.mapping.ReceiptTypeMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.IncomeResponseDto;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class IncomeCommandUseCase {

  private final IncomePersistencePort incomePersistencePort;
  private final IncomeMapper incomeMapper;
  private final ReceiptTypeMapper receiptTypeMapper;
  private final IncomeDetailService incomeDetailService;
  private final UserQueryUseCase userQueryUseCase;
  private final SupplierQueryUseCase supplierQueryUseCase;
  private final ReceiptNumberGeneratorPort receiptNumberGeneratorPort;
  private final IncomeQueryUseCase incomeQueryUseCase;

  @Transactional
  public IncomeResponseDto create(final IncomeRequestDto incomeRequestDto) {
    logIncomeStarted("income.create.started", null, incomeRequestDto);
    final IncomeEntity incomeEntity = incomeMapper.toEntity(incomeRequestDto);
    incomeEntity.setReceiptSeries(receiptNumberGeneratorPort.nextSerialNumber());
    incomeEntity.setReceiptNumber(receiptNumberGeneratorPort.nextReceiptNumber());
    incomeEntity.setIncomeUser(userQueryUseCase.getUserByEmail(incomeRequestDto.incomeUser().email()));
    incomeEntity.setReceiptType(receiptTypeMapper.toEntity(incomeRequestDto.receiptType()));
    incomeEntity.setSupplier(
        supplierQueryUseCase.findSupplierEntityByNif(incomeRequestDto.supplier().nif()));
    incomeEntity.setDeleted(Boolean.FALSE);

    saveIncomeDetails(incomeRequestDto, incomeEntity);
    final IncomeResponseDto createdIncome = incomeMapper.toDto(incomePersistencePort.save(incomeEntity));
    logIncomeCompleted("income.create.completed", createdIncome);
    return createdIncome;
  }

  @Transactional
  public IncomeResponseDto update(final Long receiptNumber, final IncomeRequestDto incomeRequestDto) {
    logIncomeStarted("income.update.started", receiptNumber, incomeRequestDto);
    final IncomeEntity incomeEntity = incomeQueryUseCase.findEntityByReceiptNumber(receiptNumber);
    incomeDetailService.restoreStock(incomeEntity.getIncomeDetails());

    incomeMapper.updateEntity(incomeRequestDto, incomeEntity);
    incomeEntity.setSupplier(
        supplierQueryUseCase.findSupplierEntityByNif(incomeRequestDto.supplier().nif()));
    incomeEntity.setReceiptType(receiptTypeMapper.toEntity(incomeRequestDto.receiptType()));
    incomeEntity.setLastModifiedBy(
        userQueryUseCase.getUserByEmail(incomeRequestDto.incomeUser().email()));

    saveIncomeDetails(incomeRequestDto, incomeEntity);
    final IncomeResponseDto updatedIncome = incomeMapper.toDto(incomePersistencePort.save(incomeEntity));
    logIncomeCompleted("income.update.completed", updatedIncome);
    return updatedIncome;
  }

  // NOTE: the legacy `delete(receiptNumber)` method was replaced by the annul flow.
  // See `AnnulIncomeUseCase` + `POST /api/v1/incomes/{id}/annul`. Hard-removing or
  // soft-flagging an income did not produce the contabilidad-grade audit trail the
  // domain expects — an annul keeps the original visible with `status = ANNULLED`
  // and creates a reversal counter-entry so the cancellation is explicit and
  // reconstructible from the income table alone.

  private void saveIncomeDetails(final IncomeRequestDto incomeRequestDto, final IncomeEntity incomeEntity) {
    if (CollectionUtils.isEmpty(incomeRequestDto.incomeDetails())) {
      incomeEntity.setIncomeDetails(List.of());
      incomeEntity.setTotalAmount(BigDecimal.ZERO);
      return;
    }

    final List<IncomeDetailEntity> mappedDetails =
        incomeDetailService.mapDetails(incomeRequestDto.incomeDetails());
    incomeEntity.setIncomeDetails(mappedDetails);
    incomeDetailService.applyStockAndRefreshPrices(incomeEntity.getIncomeDetails());
    incomeEntity.setTotalAmount(
        incomeDetailService.calculateTotal(incomeEntity.getIncomeDetails(), incomeRequestDto.tax()));
  }

  private void logIncomeStarted(
      final String event, final Long receiptNumber, final IncomeRequestDto incomeRequestDto) {
    var builder =
        log.atInfo()
            .addKeyValue("event", event)
            .addKeyValue("supplierId", incomeRequestDto.supplier().id())
            .addKeyValue("itemCount", CollectionUtils.size(incomeRequestDto.incomeDetails()));

    if (receiptNumber != null) {
      builder = builder.addKeyValue("receiptNumber", receiptNumber);
    }
    builder.log(event);
  }

  private void logIncomeCompleted(final String event, final IncomeResponseDto incomeResponseDto) {
    log.atInfo()
        .addKeyValue("event", event)
        .addKeyValue("receiptNumber", incomeResponseDto.receiptNumber())
        .addKeyValue("receiptSeries", incomeResponseDto.receiptSeries())
        .addKeyValue("totalAmount", incomeResponseDto.totalAmount())
        .log(event);
  }

}
