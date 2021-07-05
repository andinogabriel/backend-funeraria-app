package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.response.ReceiptTypeResponseDto;
import disenodesistemas.backendfunerariaapp.repository.ReceiptTypeRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.IReceiptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReceiptTypeServiceImpl implements IReceiptType {

    private final ReceiptTypeRepository receiptTypeRepository;

    @Autowired
    public ReceiptTypeServiceImpl(ReceiptTypeRepository receiptTypeRepository) {
        this.receiptTypeRepository = receiptTypeRepository;
    }

    @Override
    public List<ReceiptTypeResponseDto> getAllReceiptTypes() {
        return receiptTypeRepository.findAllByOrderByName();
    }

}
