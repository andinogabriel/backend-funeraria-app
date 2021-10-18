package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.SlideImageRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.SlideImageResponseDto;
import disenodesistemas.backendfunerariaapp.entities.SlideImageEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.SlideImageRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.IFileStore;
import disenodesistemas.backendfunerariaapp.service.Interface.ISlideImage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service @RequiredArgsConstructor
public class SlideImageServiceImpl implements ISlideImage {

    @Autowired
    private final SlideImageRepository slideImageRepository;
    private final IFileStore fileStore;
    private final ProjectionFactory projectionFactory;
    private final MessageSource messageSource;

    @Override
    public SlideImageResponseDto addNewSlideImage(SlideImageRequestDto slideImageRequestDto) {
        SlideImageEntity slideImageEntity = new SlideImageEntity(
                slideImageRequestDto.getTitle(),
                slideImageRequestDto.getDescription()
        );
        SlideImageEntity slideImageCreated = slideImageRepository.save(slideImageEntity);
        slideImageCreated.setImageLink(fileStore.save(slideImageCreated, slideImageRequestDto.getImage()));
        return projectionFactory.createProjection(SlideImageResponseDto.class, slideImageRepository.save(slideImageCreated));
    }

    @Override
    public SlideImageEntity getImageSlideById(Long id) {
        return slideImageRepository.findById(id).orElseThrow(
            () -> new AppException(
                messageSource.getMessage("slideImage.error.not.found", null, Locale.getDefault()),
                HttpStatus.NOT_FOUND
            )
        );
    }

    @Override
    public SlideImageResponseDto updateSlideImage(Long id, SlideImageRequestDto slideImageRequestDto) {
        SlideImageEntity slideImageToUpdate = getImageSlideById(id);
        slideImageToUpdate.setTitle(slideImageRequestDto.getTitle());
        slideImageToUpdate.setDescription(slideImageRequestDto.getDescription());
        if(slideImageRequestDto.getImage() != null) {
            slideImageToUpdate.setImageLink(fileStore.save(slideImageToUpdate, slideImageRequestDto.getImage()));
        }
        return projectionFactory.createProjection(SlideImageResponseDto.class, slideImageRepository.save(slideImageToUpdate));
    }

    @Override
    public void deleteSlideImage(Long id) {
        SlideImageEntity slideImageToDelete = getImageSlideById(id);
        fileStore.deleteFilesFromS3Bucket(slideImageToDelete);
        slideImageRepository.delete(slideImageToDelete);
    }

    @Override
    public List<SlideImageResponseDto> getAllSlideImages() {
        return slideImageRepository.findAllProjectedBy();
    }
}
