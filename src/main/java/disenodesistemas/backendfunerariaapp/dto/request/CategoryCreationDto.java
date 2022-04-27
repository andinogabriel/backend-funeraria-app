package disenodesistemas.backendfunerariaapp.dto.request;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public class CategoryCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;

    @NotBlank(message = "{category.error.empty.name}")
    private String name;

    private String description;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
