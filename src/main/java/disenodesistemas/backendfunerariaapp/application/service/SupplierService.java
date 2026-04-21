package disenodesistemas.backendfunerariaapp.application.service;

import disenodesistemas.backendfunerariaapp.domain.entity.SupplierEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.SupplierRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.SupplierResponseDto;
import java.util.List;

public interface SupplierService {

  SupplierResponseDto create(SupplierRequestDto dto);

  SupplierResponseDto update(String nif, SupplierRequestDto dto);

  void delete(String nif);

  List<SupplierResponseDto> findAll();

  SupplierResponseDto findById(String nif);

  SupplierEntity findSupplierEntityByNif(String nif);
}
