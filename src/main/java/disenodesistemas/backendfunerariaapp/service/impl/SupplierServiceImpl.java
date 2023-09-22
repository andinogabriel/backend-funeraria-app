package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.AddressRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.MobileNumberRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.SupplierRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.SupplierResponseDto;
import disenodesistemas.backendfunerariaapp.entities.AddressEntity;
import disenodesistemas.backendfunerariaapp.entities.MobileNumberEntity;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.SupplierRepository;
import disenodesistemas.backendfunerariaapp.service.SupplierService;
import disenodesistemas.backendfunerariaapp.service.converters.AbstractConverter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final ProjectionFactory projectionFactory;
    private final AbstractConverter<MobileNumberEntity, MobileNumberRequestDto> mobileNumberConverter;
    private final AbstractConverter<AddressEntity, AddressRequestDto> addressConverter;

    @Override
    public List<SupplierResponseDto> getSuppliers() {
        return supplierRepository.findAllProjectedByOrderByIdDesc();
    }

    @Override
    public SupplierResponseDto createSupplier(final SupplierRequestDto supplier) {
        val supplierEntity = new SupplierEntity(
                supplier.getName(),
                supplier.getNif(),
                supplier.getWebPage(),
                supplier.getEmail()
        );
        supplierEntity.setMobileNumbers(mobileNumberConverter.fromDTOs(supplier.getMobileNumbers()));
        supplierEntity.setAddresses(addressConverter.fromDTOs(supplier.getAddresses()));
        return projectionFactory.createProjection(SupplierResponseDto.class, supplierRepository.save(supplierEntity));
    }

    @Override
    public SupplierResponseDto findSupplierByNif(final String nif) {
        return projectionFactory.createProjection(SupplierResponseDto.class, findSupplierEntityByNif(nif));
    }

    @Override
    public void deleteSupplier(final String nif) {
        supplierRepository.delete(findSupplierEntityByNif(nif));
    }

    @Override
    public SupplierResponseDto updateSupplier(final String nif, final SupplierRequestDto supplier) {
        val supplierEntity = findSupplierEntityByNif(nif);

        supplierEntity.setName(supplier.getName());
        supplierEntity.setNif(supplier.getNif());
        supplierEntity.setEmail(supplier.getEmail());
        supplierEntity.setWebPage(supplier.getWebPage());

        val deletedMobileNumbers = getDeletedMobileNumbers(supplierEntity, supplier);
        deletedMobileNumbers.forEach(supplierEntity::removeMobileNumber);
        supplierEntity.setMobileNumbers(mobileNumberConverter.fromDTOs(supplier.getMobileNumbers()));

        val deletedAddresses = getDeletedAddresses(supplierEntity, supplier);
        deletedAddresses.forEach(supplierEntity::removeAddress);
        supplierEntity.setAddresses(addressConverter.fromDTOs(supplier.getAddresses()));

        return projectionFactory.createProjection(SupplierResponseDto.class, supplierRepository.save(supplierEntity));
    }

    @Override
    public SupplierEntity findSupplierEntityByNif(final String nif) {
        return supplierRepository.findByNif(nif).orElseThrow(() -> new AppException("supplier.error.not.found", HttpStatus.NOT_FOUND));
    }

    private List<MobileNumberEntity> getDeletedMobileNumbers(final SupplierEntity supplierEntity, final SupplierRequestDto supplier) {
        return !isEmpty(supplierEntity.getMobileNumbers()) ? supplierEntity.getMobileNumbers()
                .stream()
                .filter(mDb -> !supplier.getMobileNumbers().contains(mobileNumberConverter.toDTO(mDb)))
                .collect(Collectors.toUnmodifiableList())
                : List.of();
    }

    private List<AddressEntity> getDeletedAddresses(final SupplierEntity supplierEntity, final SupplierRequestDto supplier) {
        return !isEmpty(supplierEntity.getAddresses()) ? supplierEntity.getAddresses().stream()
                .filter(aDb -> !supplier.getAddresses().contains(addressConverter.toDTO(aDb)))
                .collect(Collectors.toUnmodifiableList())
                : List.of();
    }

}
