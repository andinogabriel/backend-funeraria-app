package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.SlideImageRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.SlideImageResponseDto;
import disenodesistemas.backendfunerariaapp.service.Interface.ISlideImage;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static disenodesistemas.backendfunerariaapp.utils.ApiConstants.*;

@RestController
@RequestMapping(VERSION + IMAGES_SLIDE)
@RequiredArgsConstructor
public class SlideImageController {

    @Autowired
    private final ISlideImage slideImageService;

    @PostMapping
    public ResponseEntity<SlideImageResponseDto> addNewSlideImage(@ModelAttribute(name = "slideImageRequestDto") @Valid SlideImageRequestDto slideImageRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(slideImageService.addNewSlideImage(slideImageRequestDto));
    }

    @PutMapping(path = "/{id}")
    public SlideImageResponseDto updateSlideImage(@PathVariable  Long id, @ModelAttribute(name = "slideImageRequestDto") @Valid SlideImageRequestDto slideImageRequestDto) {
        return slideImageService.updateSlideImage(id, slideImageRequestDto);
    }

    @DeleteMapping(path = "/{id}")
    public OperationStatusModel deleteSlideImage(@PathVariable Long id) {
        OperationStatusModel operationStatusModel = new OperationStatusModel();
        operationStatusModel.setName("DELETE");
        slideImageService.deleteSlideImage(id);
        operationStatusModel.setResult("SUCCESS");
        return operationStatusModel;
    }

    @GetMapping
    public List<SlideImageResponseDto> getAllImagesSlide() {
        return slideImageService.getAllSlideImages();
    }

}
