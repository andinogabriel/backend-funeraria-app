package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.AddressCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.AddressResponseDto;
import disenodesistemas.backendfunerariaapp.entities.AddressEntity;
import disenodesistemas.backendfunerariaapp.entities.CityEntity;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.repository.AddressRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.IAddress;
import disenodesistemas.backendfunerariaapp.service.Interface.ICity;
import disenodesistemas.backendfunerariaapp.service.Interface.ISupplier;
import disenodesistemas.backendfunerariaapp.service.Interface.IUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Locale;

@Service
public class AddressServiceImpl implements IAddress {

    private final AddressRepository addressRepository;
    private final ISupplier supplierService;
    private final IUser userService;
    private final ICity cityService;
    private final MessageSource messageSource;
    private final ProjectionFactory projectionFactory;

    @Autowired
    public AddressServiceImpl(AddressRepository addressRepository, ISupplier supplierService, IUser userService, ICity cityService, MessageSource messageSource, ProjectionFactory projectionFactory) {
        this.addressRepository = addressRepository;
        this.supplierService = supplierService;
        this.userService = userService;
        this.cityService = cityService;
        this.messageSource = messageSource;
        this.projectionFactory = projectionFactory;
    }


    @Override
    public AddressResponseDto createAddress(AddressCreationDto addressCreationDto) {
        SupplierEntity supplierEntity = null;
        UserEntity userEntity = null;
        if(Long.valueOf(addressCreationDto.getSupplierAddress()) != null) {
            supplierEntity = supplierService.getSupplierById(addressCreationDto.getSupplierAddress());
        } else {
            userEntity = userService.getUserById(addressCreationDto.getUserAddress());
        }

        CityEntity cityEntity = cityService.findCityById(addressCreationDto.getCity());
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setStreetName(addressCreationDto.getStreetName());
        addressEntity.setBlockStreet(addressCreationDto.getBlockStreet());
        addressEntity.setApartment(addressCreationDto.getApartment());
        addressEntity.setFlat(addressCreationDto.getFlat());
        addressEntity.setCity(cityEntity);
        if(Long.valueOf(addressCreationDto.getSupplierAddress()) != null) {
            addressEntity.setSupplierAddress(supplierEntity);
        } else {
            addressEntity.setUserAddress(userEntity);
        }

        AddressEntity createdAddress = addressRepository.save(addressEntity);
        return projectionFactory.createProjection(AddressResponseDto.class, createdAddress);
    }

    @Override
    public AddressResponseDto updateAddress(long id, AddressCreationDto addressDto) {
        AddressEntity addressEntity = getAddressById(id);
        addressEntity.setStreetName(addressDto.getStreetName());
        addressEntity.setBlockStreet(addressDto.getBlockStreet());
        addressEntity.setApartment(addressDto.getApartment());
        addressEntity.setFlat(addressDto.getFlat());
        CityEntity cityEntity = cityService.findCityById(addressDto.getCity());
        addressEntity.setCity(cityEntity);
        AddressEntity addressUpdated = addressRepository.save(addressEntity);

        return projectionFactory.createProjection(AddressResponseDto.class, addressUpdated);
    }

    @Override
    public void deleteAddress(long id) {
        AddressEntity addressEntity = getAddressById(id);
        addressRepository.delete(addressEntity);
    }

    @Override
    public AddressEntity getAddressById(long id) {
        return addressRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(
                        messageSource.getMessage("address.error.not.found", null, Locale.getDefault())
                )
        );
    }

}
