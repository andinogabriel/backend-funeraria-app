package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.BrandCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;

import java.util.List;

public interface IBrand {

    List<BrandResponseDto> getAllBrands();

    BrandEntity getBrandById(long id);

    BrandResponseDto createBrand(BrandCreationDto brandDto);

    BrandResponseDto updateBrand(long id, BrandCreationDto brandDto);

    void deleteBrand(long id);

}
