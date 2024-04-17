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
import disenodesistemas.backendfunerariaapp.service.EntityProcessor;
import disenodesistemas.backendfunerariaapp.service.SupplierService;
import disenodesistemas.backendfunerariaapp.service.converters.AbstractConverter;
import java.util.List;
import java.util.function.Function;
import lombok.val;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SupplierServiceImpl implements SupplierService {

  private final SupplierRepository supplierRepository;
  private final ProjectionFactory projectionFactory;
  private final AbstractConverter<MobileNumberEntity, MobileNumberRequestDto> mobileNumberConverter;
  private final AbstractConverter<AddressEntity, AddressRequestDto> addressConverter;
  private final EntityProcessor<MobileNumberEntity, MobileNumberRequestDto>
      mobileNumberEntityProcessor;
  private final EntityProcessor<AddressEntity, AddressRequestDto> addressEntityProcessor;

  public SupplierServiceImpl(
      final SupplierRepository supplierRepository,
      final ProjectionFactory projectionFactory,
      final AbstractConverter<MobileNumberEntity, MobileNumberRequestDto> mobileNumberConverter,
      final AbstractConverter<AddressEntity, AddressRequestDto> addressConverter) {
    this.supplierRepository = supplierRepository;
    this.projectionFactory = projectionFactory;
    this.mobileNumberConverter = mobileNumberConverter;
    this.addressConverter = addressConverter;
    this.mobileNumberEntityProcessor = new DefaultEntityProcessor<>();
    this.addressEntityProcessor = new DefaultEntityProcessor<>();
  }

  @Override
  public List<SupplierResponseDto> findAll() {
    return supplierRepository.findAllProjectedByOrderByIdDesc();
  }

  @Override
  public SupplierResponseDto create(final SupplierRequestDto supplier) {
    val supplierEntity =
        new SupplierEntity(
            supplier.getName(), supplier.getNif(), supplier.getWebPage(), supplier.getEmail());
    supplierEntity.setMobileNumbers(mobileNumberConverter.fromDTOs(supplier.getMobileNumbers()));
    supplierEntity.setAddresses(addressConverter.fromDTOs(supplier.getAddresses()));
    return projectionFactory.createProjection(
        SupplierResponseDto.class, supplierRepository.save(supplierEntity));
  }

  @Override
  public SupplierResponseDto findSupplierByNif(final String nif) {
    return projectionFactory.createProjection(
        SupplierResponseDto.class, findSupplierEntityByNif(nif));
  }

  @Override
  public void delete(final String nif) {
    supplierRepository.delete(findSupplierEntityByNif(nif));
  }

  @Override
  public SupplierResponseDto update(final String nif, final SupplierRequestDto supplier) {
    val supplierEntity = findSupplierEntityByNif(nif);

    supplierEntity.setName(supplier.getName());
    supplierEntity.setNif(supplier.getNif());
    supplierEntity.setEmail(supplier.getEmail());
    supplierEntity.setWebPage(supplier.getWebPage());

    updateMobileNumbers(supplierEntity, supplier);
    updateAddresses(supplierEntity, supplier);

    return projectionFactory.createProjection(
        SupplierResponseDto.class, supplierRepository.save(supplierEntity));
  }

  @Override
  public SupplierEntity findSupplierEntityByNif(final String nif) {
    return supplierRepository
        .findByNif(nif)
        .orElseThrow(() -> new AppException("supplier.error.not.found", HttpStatus.NOT_FOUND));
  }

  private void updateMobileNumbers(SupplierEntity supplierEntity, SupplierRequestDto supplier) {
    final Function<MobileNumberEntity, MobileNumberRequestDto> entityToDtoConverter =
        mobileNumberConverter::toDTO;
    final List<MobileNumberEntity> deletedMobileNumbers =
        mobileNumberEntityProcessor.getDeletedEntities(
            supplierEntity.getMobileNumbers(), supplier.getMobileNumbers(), entityToDtoConverter);
    deletedMobileNumbers.forEach(supplierEntity::removeMobileNumber);
    supplierEntity.setMobileNumbers(mobileNumberConverter.fromDTOs(supplier.getMobileNumbers()));
  }

  private void updateAddresses(SupplierEntity supplierEntity, SupplierRequestDto supplier) {
    final Function<AddressEntity, AddressRequestDto> entityToDtoConverter = addressConverter::toDTO;
    final List<AddressEntity> deletedAddresses =
        addressEntityProcessor.getDeletedEntities(
            supplierEntity.getAddresses(), supplier.getAddresses(), entityToDtoConverter);
    deletedAddresses.forEach(supplierEntity::removeAddress);
    supplierEntity.setAddresses(addressConverter.fromDTOs(supplier.getAddresses()));
  }
}
