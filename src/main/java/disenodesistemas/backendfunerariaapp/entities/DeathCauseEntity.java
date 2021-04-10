package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "death_causes")
@Getter @Setter
public class DeathCauseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false, length = 150)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "deceasedDeathCause")
    private List<DeceasedEntity> deceasedList = new ArrayList<>();



}
