package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.BrandRepository;
import disenodesistemas.backendfunerariaapp.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandServiceImplService implements BrandService {

    private final BrandRepository brandRepository;
    private final ProjectionFactory projectionFactory;


    @Override
    @Transactional(readOnly = true)
    public List<BrandResponseDto> getAllBrands() {
        return brandRepository.findAllByOrderByName();
    }

    @Override
    @Transactional(readOnly = true)
    public BrandEntity getBrandById(final Long id) {
        return brandRepository.findById(id).orElseThrow(() -> new AppException("brand.error.not.found",HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public BrandResponseDto createBrand(final BrandRequestDto brandDto) {
        val brandEntity = new BrandEntity(
                brandDto.getName(),
                brandDto.getWebPage()
        );
        return projectionFactory.createProjection(BrandResponseDto.class, brandRepository.save(brandEntity));
    }

    @Override
    @Transactional
    public BrandResponseDto updateBrand(final Long id, final BrandRequestDto brandDto) {
        val brandEntity = getBrandById(id);
        brandEntity.setName(brandDto.getName());
        brandEntity.setWebPage(brandDto.getWebPage());
        return projectionFactory.createProjection(BrandResponseDto.class, brandRepository.save(brandEntity));
    }

    @Override
    @Transactional
    public void deleteBrand(final Long id) {
        brandRepository.delete(getBrandById(id));
    }

}
