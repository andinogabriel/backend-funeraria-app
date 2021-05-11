package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.GenderDto;
import disenodesistemas.backendfunerariaapp.entities.GenderEntity;
import disenodesistemas.backendfunerariaapp.repository.GenderRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GenderService implements GenderServiceInterface{

    @Autowired
    GenderRepository genderRepository;

    @Autowired
    ModelMapper mapper;

    @Override
    public List<GenderDto> getGenders() {
        List<GenderEntity> genderEntity = genderRepository.findAll();
        List<GenderDto> genders = new ArrayList<>();
        for (GenderEntity gender: genderEntity) {
            GenderDto genderDto = mapper.map(gender, GenderDto.class);
            genders.add(genderDto);
        }

        return genders;
    }
}
