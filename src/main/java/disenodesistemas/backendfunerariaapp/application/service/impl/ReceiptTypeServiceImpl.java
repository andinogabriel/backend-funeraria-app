package disenodesistemas.backendfunerariaapp.application.service.impl;

import disenodesistemas.backendfunerariaapp.application.service.ReceiptTypeService;
import disenodesistemas.backendfunerariaapp.application.usecase.receipttype.ReceiptTypeQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.web.dto.response.ReceiptTypeResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReceiptTypeServiceImpl implements ReceiptTypeService {
  private final ReceiptTypeQueryUseCase receiptTypeQueryUseCase;

  @Override
  public List<ReceiptTypeResponseDto> getAllReceiptTypes() {
    return receiptTypeQueryUseCase.getAllReceiptTypes();
  }

  @Override
  public ReceiptTypeEntity findByNameIsContainingIgnoreCase(final String name) {
    return receiptTypeQueryUseCase.findByNameIsContainingIgnoreCase(name);
  }
}
