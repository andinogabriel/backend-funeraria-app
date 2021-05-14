package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.ReceiptTypeDto;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.repository.ReceiptTypeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReceiptTypeService {

    @Autowired
    ReceiptTypeRepository receiptTypeRepository;

    @Autowired
    ModelMapper mapper;

    public List<ReceiptTypeDto> getAllReceiptTypes() {
        List<ReceiptTypeEntity> receiptTypeEntities = receiptTypeRepository.findAllByOrderByName();
        List<ReceiptTypeDto> receiptTypesDto = new ArrayList<>();
        receiptTypeEntities.forEach(r -> receiptTypesDto.add(mapper.map(r, ReceiptTypeDto.class)));
        return receiptTypesDto;
    }

}
