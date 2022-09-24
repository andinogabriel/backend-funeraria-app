package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;

import java.util.List;

public interface BrandService {

    List<BrandResponseDto> getAllBrands();

    BrandEntity getBrandById(Long id);

    BrandResponseDto createBrand(BrandRequestDto brandDto);

    BrandResponseDto updateBrand(Long id, BrandRequestDto brandDto);

    void deleteBrand(Long id);

}
