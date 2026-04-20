package disenodesistemas.backendfunerariaapp.application.usecase.supplier;

import disenodesistemas.backendfunerariaapp.application.port.out.SupplierPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.AddressEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MobileNumberEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.SupplierEntity;
import disenodesistemas.backendfunerariaapp.mapping.AddressMapper;
import disenodesistemas.backendfunerariaapp.mapping.MobileNumberMapper;
import disenodesistemas.backendfunerariaapp.mapping.SupplierMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.SupplierRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.SupplierResponseDto;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierCommandUseCase {

    private final SupplierPersistencePort supplierPersistencePort;
    private final SupplierMapper supplierMapper;
    private final MobileNumberMapper mobileNumberMapper;
    private final AddressMapper addressMapper;
    private final SupplierQueryUseCase supplierQueryUseCase;

    @Transactional
    public SupplierResponseDto create(final SupplierRequestDto supplier) {
        final SupplierEntity entity = supplierMapper.toEntity(supplier);
        return supplierMapper.toDto(supplierPersistencePort.save(entity));
    }

    @Transactional
    public SupplierResponseDto update(final String nif, final SupplierRequestDto supplier) {
        final SupplierEntity entity = supplierQueryUseCase.findSupplierEntityByNif(nif);
        supplierMapper.updateEntity(supplier, entity);
        syncMobileNumbers(entity, supplier);
        syncAddresses(entity, supplier);
        return supplierMapper.toDto(supplierPersistencePort.save(entity));
    }

    @Transactional
    public void delete(final String nif) {
        supplierPersistencePort.delete(supplierQueryUseCase.findSupplierEntityByNif(nif));
    }

    private void syncMobileNumbers(final SupplierEntity entity, final SupplierRequestDto supplier) {
        final var requested = supplier.mobileNumbers() == null ? List.<MobileNumberEntity>of() : supplier.mobileNumbers().stream().map(mobileNumberMapper::toEntity).toList();

        final Set<Long> requestedIds = requested.stream().map(MobileNumberEntity::getId)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        entity.getMobileNumbers()
                .removeIf(mobileNumber -> mobileNumber.getId() != null && !requestedIds.contains(mobileNumber.getId()));
        entity.setMobileNumbers(requested);
    }

    private void syncAddresses(final SupplierEntity entity, final SupplierRequestDto supplier) {
        final var requested = supplier.addresses() == null ? List.<AddressEntity>of() : supplier.addresses().stream().
                map(addressMapper::toEntity).toList();

        final Set<Long> requestedIds = requested.stream().map(AddressEntity::getId)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        entity.getAddresses()
                .removeIf(address -> address.getId() != null && !requestedIds.contains(address.getId()));
        entity.setAddresses(requested);
    }
}
