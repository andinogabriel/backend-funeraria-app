package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.response.GenderResponseDto;
import disenodesistemas.backendfunerariaapp.entities.GenderEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.GenderRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.GenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenderServiceImplService implements GenderService {

    private final GenderRepository genderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GenderResponseDto> getGenders() {
        return genderRepository.findAllProjectedBy();
    }

    @Override
    @Transactional(readOnly = true)
    public GenderEntity getGenderById(final Long id) {
        return genderRepository.findById(id).orElseThrow(() -> new AppException("gender.error.not.found", HttpStatus.NOT_FOUND));
    }
}
