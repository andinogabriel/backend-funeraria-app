package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.AddressCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.AddressResponseDto;
import disenodesistemas.backendfunerariaapp.entities.AddressEntity;
import disenodesistemas.backendfunerariaapp.entities.CityEntity;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.AddressRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.IAddress;
import disenodesistemas.backendfunerariaapp.service.Interface.ICity;
import disenodesistemas.backendfunerariaapp.service.Interface.ISupplier;
import disenodesistemas.backendfunerariaapp.service.Interface.IUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
        CityEntity cityEntity = cityService.findCityById(addressCreationDto.getCity());

        if(addressCreationDto.getSupplierAddress() != null) {
            supplierEntity = supplierService.getSupplierById(addressCreationDto.getSupplierAddress());
        } else {
            userEntity = userService.getUserById(addressCreationDto.getUserAddress());
        }

        AddressEntity addressEntity = AddressEntity.builder()
                .apartment(addressCreationDto.getApartment())
                .blockStreet(addressCreationDto.getBlockStreet())
                .flat(addressCreationDto.getFlat())
                .city(cityEntity)
                .supplierAddress(supplierEntity)
                .userAddress(userEntity)
                .build();

        AddressEntity createdAddress = addressRepository.save(addressEntity);
        return projectionFactory.createProjection(AddressResponseDto.class, createdAddress);
    }

    @Override
    public AddressResponseDto updateAddress(Long id, AddressCreationDto addressDto) {
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
    public void deleteAddress(Long id) {
        AddressEntity addressEntity = getAddressById(id);
        addressRepository.delete(addressEntity);
    }

    @Override
    public AddressEntity getAddressById(Long id) {
        return addressRepository.findById(id).orElseThrow(
                () -> new AppException(
                        messageSource.getMessage("address.error.not.found", null, Locale.getDefault()),
                        HttpStatus.NOT_FOUND
                )
        );
    }

}
