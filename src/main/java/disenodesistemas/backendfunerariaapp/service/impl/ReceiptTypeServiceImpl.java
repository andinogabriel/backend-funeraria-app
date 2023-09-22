package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.response.ReceiptTypeResponseDto;
import disenodesistemas.backendfunerariaapp.repository.ReceiptTypeRepository;
import disenodesistemas.backendfunerariaapp.service.ReceiptTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptTypeServiceImpl implements ReceiptTypeService {

    private final ReceiptTypeRepository receiptTypeRepository;

    @Override
    public List<ReceiptTypeResponseDto> getAllReceiptTypes() {
        return receiptTypeRepository.findAllByOrderByName();
    }

}
