package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.AddressCreationDto;
import disenodesistemas.backendfunerariaapp.dto.AddressDto;
import disenodesistemas.backendfunerariaapp.entities.AddressEntity;
import disenodesistemas.backendfunerariaapp.entities.CityEntity;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import disenodesistemas.backendfunerariaapp.repository.AddressRepository;
import disenodesistemas.backendfunerariaapp.repository.CityRepository;
import disenodesistemas.backendfunerariaapp.repository.SupplierRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    SupplierRepository supplierRepository;

    @Autowired
    CityRepository cityRepository;

    @Autowired
    ModelMapper mapper;

    public AddressDto createAddress(AddressCreationDto addressCreationDto) {
        SupplierEntity supplierEntity = supplierRepository.findById(addressCreationDto.getSupplierAddress());
        CityEntity cityEntity = cityRepository.findById(addressCreationDto.getCity());

        AddressEntity addressEntity = new AddressEntity();

        addressEntity.setStreetName(addressCreationDto.getStreetName());
        addressEntity.setBlockStreet(addressCreationDto.getBlockStreet());
        addressEntity.setApartment(addressCreationDto.getApartment());
        addressEntity.setFlat(addressCreationDto.getFlat());
        addressEntity.setCity(cityEntity);
        addressEntity.setSupplierAddress(supplierEntity);

        AddressEntity createdAddress = addressRepository.save(addressEntity);

        AddressDto addressDto = mapper.map(createdAddress, AddressDto.class);
        return addressDto;
    }

}
