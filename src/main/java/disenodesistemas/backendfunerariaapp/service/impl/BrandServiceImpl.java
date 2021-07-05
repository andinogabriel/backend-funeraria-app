package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.BrandCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.repository.BrandRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.IBrand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Locale;

@Service
public class BrandServiceImpl implements IBrand {

    private final BrandRepository brandRepository;
    private final MessageSource messageSource;
    private final ProjectionFactory projectionFactory;

    @Autowired
    public BrandServiceImpl(BrandRepository brandRepository, MessageSource messageSource, ProjectionFactory projectionFactory) {
        this.brandRepository = brandRepository;
        this.messageSource = messageSource;
        this.projectionFactory = projectionFactory;
    }


    @Override
    public List<BrandResponseDto> getAllBrands() {
        return brandRepository.findAllByOrderByName();
    }

    @Override
    public BrandEntity getBrandById(long id) {
        return brandRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(
                        messageSource.getMessage("brand.error.not.found", null, Locale.getDefault())
                )
        );
    }

    @Override
    public BrandResponseDto createBrand(BrandCreationDto brandDto) {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName(brandDto.getName());
        brandEntity.setWebPage(brandDto.getWebPage());
        BrandEntity createdBrand = brandRepository.save(brandEntity);
        return projectionFactory.createProjection(BrandResponseDto.class, createdBrand);
    }

    @Override
    public BrandResponseDto updateBrand(long id, BrandCreationDto brandDto) {
        BrandEntity brandEntity = getBrandById(id);
        brandEntity.setName(brandDto.getName());
        brandEntity.setWebPage(brandDto.getWebPage());
        BrandEntity updatedBrand = brandRepository.save(brandEntity);
        return projectionFactory.createProjection(BrandResponseDto.class, updatedBrand);
    }

    @Override
    public void deleteBrand(long id) {
        BrandEntity brandEntity = getBrandById(id);
        brandRepository.delete(brandEntity);
    }

}
