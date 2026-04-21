package disenodesistemas.backendfunerariaapp.application.usecase.supplier;

import disenodesistemas.backendfunerariaapp.application.port.out.SupplierPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.SupplierEntity;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.mapping.SupplierMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.SupplierResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierQueryUseCase {

  private final SupplierPersistencePort supplierPersistencePort;
  private final SupplierMapper supplierMapper;

  @Transactional(readOnly = true)
  public List<SupplierResponseDto> findAll() {
    return supplierPersistencePort.findAllByOrderByIdDesc().stream()
        .map(supplierMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public SupplierResponseDto findById(final String nif) {
    return supplierMapper.toDto(findSupplierEntityByNif(nif));
  }

  @Transactional(readOnly = true)
  public SupplierEntity findSupplierEntityByNif(final String nif) {
    return supplierPersistencePort
        .findByNif(nif)
        .orElseThrow(() -> new AppException("supplier.error.not.found", HttpStatus.NOT_FOUND));
  }
}
