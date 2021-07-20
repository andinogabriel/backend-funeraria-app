package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.response.ProvinceResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.ProvinceRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.IProvince;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ProvinceServiceImpl implements IProvince {

    private final ProvinceRepository provinceRepository;
    private final MessageSource messageSource;

    @Autowired
    public ProvinceServiceImpl(ProvinceRepository provinceRepository, MessageSource messageSource) {
        this.provinceRepository = provinceRepository;
        this.messageSource = messageSource;
    }

    @Override
    public List<ProvinceResponseDto> getAllProvinces() {
        return provinceRepository.findAllByOrderByName();
    }

    @Override
    public ProvinceEntity getProvinceById(Long id) {
        return provinceRepository.findById(id).orElseThrow(
                () -> new AppException(
                        messageSource.getMessage("province.error.not.found", null, Locale.getDefault()),
                        HttpStatus.NOT_FOUND
                )
        );
    }

}
