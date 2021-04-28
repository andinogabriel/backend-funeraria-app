package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.MobileNumberDto;
import disenodesistemas.backendfunerariaapp.dto.SupplierDto;
import disenodesistemas.backendfunerariaapp.entities.MobileNumberEntity;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import disenodesistemas.backendfunerariaapp.repository.AddressRepository;
import disenodesistemas.backendfunerariaapp.repository.MobileNumberRepository;
import disenodesistemas.backendfunerariaapp.repository.SupplierRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

        SupplierDto supplierDto = mapper.map(createdSupplier, SupplierDto.class);

        return supplierDto;
    }

    public SupplierDto getSupplierById(long id) {
        SupplierEntity supplierEntity = supplierRepository.findById(id);
        SupplierDto supplierDto = mapper.map(supplierEntity, SupplierDto.class);
        return supplierDto;
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

        SupplierDto supplierDto = mapper.map(updatedSupplier, SupplierDto.class);

        return supplierDto;
    }

    public List<MobileNumberDto> getSupplierNumbers(long supplierNumber) {
        SupplierEntity supplierEntity = supplierRepository.findById(supplierNumber);

        List<MobileNumberEntity> mobileNumberEntities = mobileNumberRepository.findBySupplierNumber(supplierEntity);

        List<MobileNumberDto> mobileNumbersDto = new ArrayList<>();

        for (MobileNumberEntity mobileNumber : mobileNumberEntities) {
            MobileNumberDto mobileNumberDto = mapper.map(mobileNumber, MobileNumberDto.class);
            mobileNumbersDto.add(mobileNumberDto);
        }
        return mobileNumbersDto;
    }

    public Page<SupplierDto> getSuppliersPaginated(int page, int limit, String sortBy, String sortDir) {
        if (page > 0) {
            page = page - 1;
        }

        List<SupplierDto> suppliersDto = new ArrayList<>();
        Pageable pageable = PageRequest.of(
                page, limit,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending()
        );
        Page<SupplierEntity> suppliersPage = supplierRepository.findAll(pageable);
        Page<SupplierDto> pagesDto = mapper.map(suppliersPage, Page.class);

        //List<SupplierEntity> suppliers = suppliersPage.getContent();

        /*
        for (SupplierEntity supplier : suppliers) {
            SupplierDto supplierDto = mapper.map(supplier, SupplierDto.class);
            suppliersDto.add(supplierDto);
        }*/
        return pagesDto;
    }

    public Page<SupplierDto> getSuppliersByName(String name, int page, int limit, String sortBy, String sortDir) {
        if (page > 0) {
            page = page - 1;
        }

        Pageable pageable = PageRequest.of(
                page, limit,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending()
        );

        Page<SupplierEntity> suppliersPage = supplierRepository.findByNameContaining(pageable, name);
        Page<SupplierDto> pagesDto = mapper.map(suppliersPage, Page.class);
        return pagesDto;
    }

}
