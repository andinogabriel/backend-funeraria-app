package disenodesistemas.backendfunerariaapp.application.service.impl;

import disenodesistemas.backendfunerariaapp.application.service.SupplierService;
import disenodesistemas.backendfunerariaapp.application.usecase.supplier.SupplierCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.supplier.SupplierQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.SupplierEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.SupplierRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.SupplierResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

  private final SupplierCommandUseCase supplierCommandUseCase;
  private final SupplierQueryUseCase supplierQueryUseCase;

  @Override
  public List<SupplierResponseDto> findAll() {
    return supplierQueryUseCase.findAll();
  }

  @Override
  public SupplierResponseDto create(final SupplierRequestDto supplier) {
    return supplierCommandUseCase.create(supplier);
  }

  @Override
  public SupplierResponseDto findById(final String nif) {
    return supplierQueryUseCase.findById(nif);
  }

  @Override
  public void delete(final String nif) {
    supplierCommandUseCase.delete(nif);
  }

  @Override
  public SupplierResponseDto update(final String nif, final SupplierRequestDto supplier) {
    return supplierCommandUseCase.update(nif, supplier);
  }

  @Override
  public SupplierEntity findSupplierEntityByNif(final String nif) {
    return supplierQueryUseCase.findSupplierEntityByNif(nif);
  }
}

