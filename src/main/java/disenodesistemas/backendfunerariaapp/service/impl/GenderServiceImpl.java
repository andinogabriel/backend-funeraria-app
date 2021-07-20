package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.response.GenderResponseDto;
import disenodesistemas.backendfunerariaapp.entities.GenderEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.GenderRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.IGender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class GenderServiceImpl implements IGender {

    private final GenderRepository genderRepository;
    private final MessageSource messageSource;

    @Autowired
    public GenderServiceImpl(GenderRepository genderRepository, MessageSource messageSource) {
        this.genderRepository = genderRepository;
        this.messageSource = messageSource;
    }


    @Override
    public List<GenderResponseDto> getGenders() {
        return genderRepository.findAllProjectedBy();
    }

    @Override
    public GenderEntity getGenderById(Long id) {
        return genderRepository.findById(id).orElseThrow(
                () -> new AppException(
                        messageSource.getMessage("gender.error.not.found", null, Locale.getDefault()),
                        HttpStatus.NOT_FOUND
                )
        );
    }
}
