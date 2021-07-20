package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.SupplierCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.SupplierResponseDto;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.SupplierRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.ISupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class SupplierServiceImpl implements ISupplier {

    private final SupplierRepository supplierRepository;
    private final MessageSource messageSource;
    private final ProjectionFactory projectionFactory;

    @Autowired
    public SupplierServiceImpl(SupplierRepository supplierRepository, MessageSource messageSource, ProjectionFactory projectionFactory) {
        this.supplierRepository = supplierRepository;
        this.messageSource = messageSource;
        this.projectionFactory = projectionFactory;
    }

    @Override
    public List<SupplierResponseDto> getSuppliers() {
        return supplierRepository.findAllProjectedBy();
    }

    @Override
    public SupplierResponseDto createSupplier(SupplierCreationDto supplier) {
        SupplierEntity supplierEntity = new SupplierEntity(
            supplier.getName(),
            supplier.getNif(),
            supplier.getWebPage(),
            supplier.getEmail()
        );
        SupplierEntity createdSupplier = supplierRepository.save(supplierEntity);
        return projectionFactory.createProjection(SupplierResponseDto.class, createdSupplier);
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

        SupplierEntity updatedSupplier = supplierRepository.save(supplierEntity);
        return projectionFactory.createProjection(SupplierResponseDto.class, updatedSupplier);
    }

}
