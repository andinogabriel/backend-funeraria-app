package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.MobileNumberCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.MobileNumberResponseDto;
import disenodesistemas.backendfunerariaapp.entities.MobileNumberEntity;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.MobileNumberRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.IMobileNumber;
import disenodesistemas.backendfunerariaapp.service.Interface.ISupplier;
import disenodesistemas.backendfunerariaapp.service.Interface.IUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MobileNumberServiceImpl implements IMobileNumber {

    private final MobileNumberRepository mobileNumberRepository;
    private final ISupplier supplierService;
    private final MessageSource messageSource;
    private final IUser userService;
    private final ProjectionFactory projectionFactory;

    @Autowired
    public MobileNumberServiceImpl(MobileNumberRepository mobileNumberRepository, ISupplier supplierService, MessageSource messageSource, IUser userService, ProjectionFactory projectionFactory) {
        this.mobileNumberRepository = mobileNumberRepository;
        this.supplierService = supplierService;
        this.messageSource = messageSource;
        this.userService = userService;
        this.projectionFactory = projectionFactory;
    }

    @Override
    public MobileNumberResponseDto createMobileNumber(MobileNumberCreationDto mobileNumber) {
        MobileNumberEntity mobileNumberEntity = new MobileNumberEntity();
        mobileNumberEntity.setMobileNumber(mobileNumber.getMobileNumber());
        if(mobileNumber.getMobileNumber() != null) {
            UserEntity userEntity = userService.getUserById(mobileNumber.getUserNumber());
            mobileNumberEntity.setUserNumber(userEntity);
        } else {
            SupplierEntity supplierEntity = supplierService.getSupplierById(mobileNumber.getSupplierNumber());
            mobileNumberEntity.setSupplierNumber(supplierEntity);
        }
        MobileNumberEntity mobileNumberCreated = mobileNumberRepository.save(mobileNumberEntity);
        return projectionFactory.createProjection(MobileNumberResponseDto.class, mobileNumberCreated);

    }

    @Override
    public MobileNumberResponseDto updateMobileNumber(Long id, MobileNumberCreationDto mobileNumberDto) {
        MobileNumberEntity mobileNumberEntity = getMobileNumberById(id);
        mobileNumberEntity.setMobileNumber(mobileNumberDto.getMobileNumber());
        MobileNumberEntity numberUpdated = mobileNumberRepository.save(mobileNumberEntity);
        return projectionFactory.createProjection(MobileNumberResponseDto.class, numberUpdated);
    }

    @Override
    public void deleteMobileNumber(Long id) {
        MobileNumberEntity mobileNumberEntity = getMobileNumberById(id);
        mobileNumberRepository.delete(mobileNumberEntity);
    }

    @Override
    public MobileNumberEntity getMobileNumberById(Long id) {
        return mobileNumberRepository.findById(id).orElseThrow(
                () -> new AppException(
                        messageSource.getMessage("mobileNumber.error.not.found", null, Locale.getDefault()),
                        HttpStatus.NOT_FOUND
                )
        );
    }

}
