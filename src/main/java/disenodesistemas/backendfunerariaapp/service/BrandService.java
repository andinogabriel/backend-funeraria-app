package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.BrandDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.repository.BrandRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Page<BrandDto> getBrandsPaginated(int page, int limit, String sortBy, String sortDir) {
        if (page > 0) {
            page = page - 1;
        }

        Pageable pageable = PageRequest.of(
                page, limit,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending()
        );

        Page<BrandEntity> brandEntities = brandRepository.findAll(pageable);
        return mapper.map(brandEntities, Page.class);
    }

    public BrandDto getBrandById(long id) {
        BrandEntity brandEntity = brandRepository.findById(id);
        return mapper.map(brandEntity, BrandDto.class);
    }

    public BrandDto createBrand(BrandDto brandDto) {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName(brandDto.getName());
        BrandEntity createdBrand = brandRepository.save(brandEntity);
        return mapper.map(createdBrand, BrandDto.class);
    }

    public BrandDto updateBrand(long id, BrandDto brandDto) {
        BrandEntity brandEntity = brandRepository.findById(id);
        brandEntity.setName(brandDto.getName());
        BrandEntity updatedBrand = brandRepository.save(brandEntity);
        return mapper.map(updatedBrand, BrandDto.class);
    }

    public void deleteBrand(long id) {
        BrandEntity brandEntity = brandRepository.findById(id);
        brandRepository.delete(brandEntity);
    }

    public Page<BrandDto> getBrandsByName(String name, int page, int limit, String sortBy, String sortDir) {
        if (page > 0) {
            page = page - 1;
        }

        Pageable pageable = PageRequest.of(
                page, limit,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending()
        );

        Page<BrandEntity> brandEntities = brandRepository.findByNameContaining(pageable, name);
        return mapper.map(brandEntities, Page.class);
    }

}
