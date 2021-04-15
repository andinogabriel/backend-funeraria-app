package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.SupplierDto;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import disenodesistemas.backendfunerariaapp.repository.AddressRepository;
import disenodesistemas.backendfunerariaapp.repository.MobileNumberRepository;
import disenodesistemas.backendfunerariaapp.repository.SupplierRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SupplierService {

    @Autowired
    SupplierRepository supplierRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    MobileNumberRepository mobileNumberRepository;

    @Autowired
    ModelMapper mapper;

    public SupplierDto createSupplier(SupplierDto supplier) {
        SupplierEntity supplierEntity = new SupplierEntity();

        supplierEntity.setEmail(supplier.getEmail());
        supplierEntity.setName(supplier.getName());
        supplierEntity.setNif(supplier.getNif());
        supplierEntity.setWebPage(supplier.getWebPage());

        SupplierEntity createdSupplier = supplierRepository.save(supplierEntity);

        SupplierDto supplierDto = mapper.map(supplierEntity, SupplierDto.class);

        return supplierDto;
    }

    public SupplierDto getSupplierById(long id) {
        SupplierEntity supplierEntity = supplierRepository.findById(id);
        SupplierDto supplierDto = mapper.map(supplierEntity, SupplierDto.class);
        return supplierDto;
    }

    public List<SupplierDto> getSuppliers() {

        List<SupplierEntity> supplierEntities = supplierRepository.findAll();

        List<SupplierDto> suppliersDto = new ArrayList<>();

        for (SupplierEntity supplier : supplierEntities) {
            SupplierDto supplierDto = mapper.map(supplier, SupplierDto.class);
            suppliersDto.add(supplierDto);
        }
        return suppliersDto;
    }

    public void deleteSupplier(long id) {
        SupplierEntity supplierEntity = supplierRepository.findById(id);

        supplierRepository.delete(supplierEntity);
    }

    public SupplierDto updateSupplier(long id, SupplierDto supplier) {
        SupplierEntity supplierEntity = supplierRepository.findById(id);

        supplierEntity.setName(supplier.getName());
        supplierEntity.setNif(supplier.getNif());
        supplierEntity.setEmail(supplier.getEmail());
        supplierEntity.setWebPage(supplier.getWebPage());

        SupplierEntity updatedSupplier = supplierRepository.save(supplierEntity);

        SupplierDto supplierDto = mapper.map(supplierEntity, SupplierDto.class);

        return supplierDto;
    }


}
