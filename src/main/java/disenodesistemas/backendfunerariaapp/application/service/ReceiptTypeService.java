package disenodesistemas.backendfunerariaapp.application.service;

import disenodesistemas.backendfunerariaapp.web.dto.response.ReceiptTypeResponseDto;
import disenodesistemas.backendfunerariaapp.domain.entity.ReceiptTypeEntity;

import java.util.List;

public interface ReceiptTypeService {

    List<ReceiptTypeResponseDto> getAllReceiptTypes();
    ReceiptTypeEntity findByNameIsContainingIgnoreCase(String name);

}
