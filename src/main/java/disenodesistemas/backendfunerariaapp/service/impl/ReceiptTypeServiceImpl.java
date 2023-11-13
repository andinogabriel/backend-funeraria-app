package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.response.ReceiptTypeResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.ReceiptTypeRepository;
import disenodesistemas.backendfunerariaapp.service.ReceiptTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptTypeServiceImpl implements ReceiptTypeService {

    private final ReceiptTypeRepository receiptTypeRepository;

    @Override
    public List<ReceiptTypeResponseDto> getAllReceiptTypes() {
        return receiptTypeRepository.findAllByOrderByName();
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptTypeEntity findByNameIsContainingIgnoreCase(final String name) {
        return receiptTypeRepository.findByNameIsContainingIgnoreCase(name)
                .orElseThrow(() -> new NotFoundException("receiptType.error.name.not.found "));
    }

}
