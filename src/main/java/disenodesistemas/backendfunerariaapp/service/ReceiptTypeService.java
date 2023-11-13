package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.response.ReceiptTypeResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;

import java.util.List;
import java.util.Optional;

public interface ReceiptTypeService {

    List<ReceiptTypeResponseDto> getAllReceiptTypes();
    ReceiptTypeEntity findByNameIsContainingIgnoreCase(String name);

}
