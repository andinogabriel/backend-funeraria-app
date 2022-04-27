package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.SlideImageRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.SlideImageResponseDto;
import disenodesistemas.backendfunerariaapp.entities.SlideImageEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ISlideImage {

    SlideImageResponseDto addNewSlideImage(SlideImageRequestDto slideImageRequestDto);

    SlideImageEntity getImageSlideById(Long id);

    SlideImageResponseDto updateSlideImage(Long id, SlideImageRequestDto slideImageRequestDto);

    void deleteSlideImage(Long id);

    List<SlideImageResponseDto> getAllSlideImages();

    void uploadImage(Long id, MultipartFile image);

}
