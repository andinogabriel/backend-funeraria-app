package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.MobileNumberCreationDto;
import disenodesistemas.backendfunerariaapp.dto.MobileNumberDto;
import disenodesistemas.backendfunerariaapp.entities.MobileNumberEntity;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import disenodesistemas.backendfunerariaapp.repository.MobileNumberRepository;
import disenodesistemas.backendfunerariaapp.repository.SupplierRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MobileNumberService {

    @Autowired
    MobileNumberRepository mobileNumberRepository;

    @Autowired
    SupplierRepository supplierRepository;

    @Autowired
    ModelMapper mapper;

    public MobileNumberDto createMobileNumber(MobileNumberCreationDto mobileNumber) {
        SupplierEntity supplierEntity = supplierRepository.findById(mobileNumber.getSupplierNumber());
        MobileNumberEntity mobileNumberEntity = new MobileNumberEntity();
        mobileNumberEntity.setMobileNumber(mobileNumber.getMobileNumber());
        mobileNumberEntity.setSupplierNumber(supplierEntity);
        MobileNumberEntity mobileNumberCreated = mobileNumberRepository.save(mobileNumberEntity);
        return mapper.map(mobileNumberCreated, MobileNumberDto.class);

    }

    public MobileNumberDto updateMobileNumber(long id, MobileNumberDto mobileNumberDto) {
        MobileNumberEntity mobileNumberEntity = mobileNumberRepository.findById(id);
        mobileNumberEntity.setMobileNumber(mobileNumberDto.getMobileNumber());
        MobileNumberEntity numberUpdated = mobileNumberRepository.save(mobileNumberEntity);
        return mapper.map(numberUpdated, MobileNumberDto.class);
    }

    public void deleteMobileNumber(long id) {
        MobileNumberEntity mobileNumberEntity = mobileNumberRepository.findById(id);
        mobileNumberRepository.delete(mobileNumberEntity);
    }



}
