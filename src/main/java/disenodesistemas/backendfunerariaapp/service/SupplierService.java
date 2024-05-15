package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.request.SupplierRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.SupplierResponseDto;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;

public interface SupplierService
    extends CommonService<SupplierResponseDto, SupplierRequestDto, String> {

  SupplierResponseDto findById(String nif);

  SupplierEntity findSupplierEntityByNif(String nif);
}
