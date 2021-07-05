package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.AddressDto;
import disenodesistemas.backendfunerariaapp.dto.request.AddressCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.AddressResponseDto;
import disenodesistemas.backendfunerariaapp.entities.AddressEntity;

public interface IAddress {

    AddressResponseDto createAddress(AddressCreationDto addressCreationDto);

    AddressResponseDto updateAddress(long id, AddressCreationDto addressDto);

    void deleteAddress(long id);

    AddressEntity getAddressById(long id);

}
