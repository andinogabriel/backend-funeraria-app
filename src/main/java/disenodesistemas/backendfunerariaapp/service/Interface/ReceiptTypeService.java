package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.response.ReceiptTypeResponseDto;

import java.util.List;

public interface ReceiptTypeService {

    List<ReceiptTypeResponseDto> getAllReceiptTypes();

}