package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.SupplierDto;
import disenodesistemas.backendfunerariaapp.dto.request.SupplierCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.SupplierResponseDto;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;

import java.util.List;

public interface ISupplier {

    List<SupplierResponseDto> getSuppliers();

    SupplierResponseDto createSupplier(SupplierCreationDto supplier);

    SupplierEntity getSupplierById(long id);

    void deleteSupplier(long id);

    SupplierResponseDto updateSupplier(long id, SupplierCreationDto supplier);



}
