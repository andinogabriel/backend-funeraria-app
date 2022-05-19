package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.AddressCreationDto;
import disenodesistemas.backendfunerariaapp.dto.request.MobileNumberCreationDto;
import disenodesistemas.backendfunerariaapp.dto.request.SupplierCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.SupplierResponseDto;
import disenodesistemas.backendfunerariaapp.entities.AddressEntity;
import disenodesistemas.backendfunerariaapp.entities.MobileNumberEntity;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.SupplierRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.ISupplier;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.context.MessageSource;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class SupplierServiceImpl implements ISupplier {

    private final SupplierRepository supplierRepository;
    private final MessageSource messageSource;
    private final ProjectionFactory projectionFactory;
    private final ModelMapper modelMapper;

    @Override
    public List<SupplierResponseDto> getSuppliers() {
        return supplierRepository.findAllProjectedByOrderByIdDesc();
    }

    @Override
    public SupplierResponseDto createSupplier(SupplierCreationDto supplier) {
        SupplierEntity supplierEntity = new SupplierEntity(
            supplier.getName(),
            supplier.getNif(),
            supplier.getWebPage(),
            supplier.getEmail()
        );

        if (supplier.getMobileNumbers() != null && supplier.getMobileNumbers().size() > 0) {
            supplierEntity.setMobileNumbers(modelMapper.map(supplier.getMobileNumbers(), new TypeToken<List<MobileNumberEntity>>() {}.getType()));
        }
        if (supplier.getAddresses() != null && supplier.getAddresses().size() > 0) {
            supplierEntity.setAddresses(modelMapper.map(supplier.getAddresses(), new TypeToken<List<AddressEntity>>() {}.getType()));
        }
        return projectionFactory.createProjection(SupplierResponseDto.class, supplierRepository.save(supplierEntity));
    }

    @Override
    public SupplierEntity getSupplierById(Long id) {
        return supplierRepository.findById(id).orElseThrow(
                () -> new AppException(
                        messageSource.getMessage("supplier.error.not.found", null, Locale.getDefault()),
                        HttpStatus.NOT_FOUND
                )
        );
    }

    @Override
    public void deleteSupplier(Long id) {
        SupplierEntity supplierEntity = getSupplierById(id);
        supplierRepository.delete(supplierEntity);
    }

    @Override
    public SupplierResponseDto updateSupplier(Long id, SupplierCreationDto supplier) {
        SupplierEntity supplierEntity = getSupplierById(id);

        supplierEntity.setName(supplier.getName());
        supplierEntity.setNif(supplier.getNif());
        supplierEntity.setEmail(supplier.getEmail());
        supplierEntity.setWebPage(supplier.getWebPage());

        if (supplier.getMobileNumbers() != null && supplier.getMobileNumbers().size() > 0) {
            List<MobileNumberEntity> deletedMobileNumbers = getDeletedMobileNumbers(supplierEntity, supplier);
            deletedMobileNumbers.forEach(supplierEntity::removeMobileNumber);

            supplierEntity.setMobileNumbers(modelMapper.map(supplier.getMobileNumbers(), new TypeToken<List<MobileNumberEntity>>() {}.getType()));
        }
        if (supplier.getAddresses() != null && supplier.getAddresses().size() > 0) {
            List<AddressEntity> deletedAddresses = getDeletedAddresses(supplierEntity, supplier);
            deletedAddresses.forEach(supplierEntity::removeAddress);
            supplierEntity.setAddresses(modelMapper.map(supplier.getAddresses(), new TypeToken<List<AddressEntity>>() {}.getType()));
        }
        return projectionFactory.createProjection(SupplierResponseDto.class, supplierRepository.save(supplierEntity));
    }


    private List<MobileNumberEntity> getDeletedMobileNumbers(SupplierEntity supplierEntity, SupplierCreationDto supplier) {
        return supplierEntity.getMobileNumbers()
                .stream()
                .filter(mDb -> !supplier.getMobileNumbers().contains(modelMapper.map(mDb, MobileNumberCreationDto.class)))
                .collect(Collectors.toList());
    }

    private List<AddressEntity> getDeletedAddresses(SupplierEntity supplierEntity, SupplierCreationDto supplier) {
        return supplierEntity.getAddresses()
                .stream()
                .filter(aDb -> !supplier.getAddresses().contains(modelMapper.map(aDb, AddressCreationDto.class)))
                .collect(Collectors.toList());
    }

}
