package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "slide_image")
@Getter
@Setter
@NoArgsConstructor
public class SlideImageEntity {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 85)
    private String title;

    @Column(nullable = false)
    private String description;

    private String imageLink;

    public SlideImageEntity(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
