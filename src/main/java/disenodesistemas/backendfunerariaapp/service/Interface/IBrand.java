package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.BrandCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;

import java.util.List;

public interface IBrand {

    List<BrandResponseDto> getAllBrands();

    BrandEntity getBrandById(Long id);

    BrandResponseDto createBrand(BrandCreationDto brandDto);

    BrandResponseDto updateBrand(Long id, BrandCreationDto brandDto);

    void deleteBrand(Long id);

}
