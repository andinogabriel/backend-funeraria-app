package disenodesistemas.backendfunerariaapp.dto.request;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public class BrandCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "{brand.error.blank.name}")
    private String name;

    private String webPage;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebPage() {
        return webPage;
    }

    public void setWebPage(String webPage) {
        this.webPage = webPage;
    }
}
