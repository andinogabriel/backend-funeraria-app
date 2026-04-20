package disenodesistemas.backendfunerariaapp.application.usecase.receipttype;

import disenodesistemas.backendfunerariaapp.application.port.out.ReceiptTypePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.ReceiptTypeMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.ReceiptTypeResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReceiptTypeQueryUseCase {

  private final ReceiptTypePersistencePort receiptTypePersistencePort;
  private final ReceiptTypeMapper receiptTypeMapper;

  public List<ReceiptTypeResponseDto> getAllReceiptTypes() {
    return receiptTypePersistencePort.findAllByOrderByName().stream()
        .map(receiptTypeMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public ReceiptTypeEntity findByNameIsContainingIgnoreCase(final String name) {
    return receiptTypePersistencePort
        .findByNameIsContainingIgnoreCase(name)
        .orElseThrow(() -> new NotFoundException("receiptType.error.name.not.found "));
  }
}
