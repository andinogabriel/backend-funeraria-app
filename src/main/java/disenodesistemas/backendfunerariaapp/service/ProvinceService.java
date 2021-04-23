package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.ProvinceDto;
import disenodesistemas.backendfunerariaapp.entities.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.repository.ProvinceRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProvinceService {

    @Autowired
    ProvinceRepository provinceRepository;

    @Autowired
    ModelMapper mapper;

    public List<ProvinceDto> getAllProvinces() {
        List<ProvinceEntity> provinceEntities = provinceRepository.findAllByOrderByName();
        List<ProvinceDto> provincesDto = new ArrayList<>();
        for (ProvinceEntity province : provinceEntities) {
            ProvinceDto provinceDto = mapper.map(province, ProvinceDto.class);
            provincesDto.add(provinceDto);
        }
        return provincesDto;
    }

}
