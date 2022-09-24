package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.SupplierRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.SupplierResponseDto;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;

import java.util.List;

public interface SupplierService {

    List<SupplierResponseDto> getSuppliers();

    SupplierResponseDto createSupplier(SupplierRequestDto supplier);

    SupplierResponseDto findSupplierByNif(String nif);
    SupplierEntity findSupplierEntityByNif(String nif);

    void deleteSupplier(String nif);

    SupplierResponseDto updateSupplier(String nif, SupplierRequestDto supplier);



}
