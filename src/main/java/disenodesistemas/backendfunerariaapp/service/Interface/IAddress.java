package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.AddressCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.AddressResponseDto;
import disenodesistemas.backendfunerariaapp.entities.AddressEntity;

public interface IAddress {

    //AddressResponseDto createAddress(AddressCreationDto addressCreationDto);

    //AddressResponseDto updateAddress(Long id, AddressCreationDto addressDto);

    void deleteAddress(Long id);

    AddressEntity getAddressById(Long id);

}
