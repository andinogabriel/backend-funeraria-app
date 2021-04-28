package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.BrandDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.repository.BrandRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BrandService {

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    ModelMapper mapper;

    public List<BrandDto> getAllBrands() {
        List<BrandEntity> brandEntities = brandRepository.findAllByOrderByName();
        List<BrandDto> brandesDto = new ArrayList<>();
        brandEntities.forEach(brandEntity -> {
            BrandDto brandDto = mapper.map(brandEntity, BrandDto.class);
            brandesDto.add(brandDto);
        });
        return brandesDto;
    }

    public BrandDto createBrand(BrandDto brandDto) {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName(brandDto.getName());
        BrandEntity createdBrand = brandRepository.save(brandEntity);
        BrandDto brandToReturn = mapper.map(createdBrand, BrandDto.class);
        return brandToReturn;
    }

    public BrandDto updateBrand(long id, BrandDto brandDto) {
        BrandEntity brandEntity = brandRepository.findById(id);
        brandEntity.setName(brandDto.getName());
        BrandEntity updatedBrand = brandRepository.save(brandEntity);
        BrandDto brandToReturn = mapper.map(updatedBrand, BrandDto.class);
        return brandToReturn;
    }

    public void deleteBrand(long id) {
        BrandEntity brandEntity = brandRepository.findById(id);
        brandRepository.delete(brandEntity);
    }

}
