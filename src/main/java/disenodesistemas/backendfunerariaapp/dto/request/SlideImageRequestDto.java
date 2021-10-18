package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter @Setter
public class SlideImageRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{slideImage.error.blank.name}")
    private String title;

    @NotBlank(message = "{slideImage.error.blank.description}")
    private String description;

    @NotNull(message = "{slideImage.error.null.image}")
    private MultipartFile image;

}
