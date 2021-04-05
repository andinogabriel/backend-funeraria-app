package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(name =  "genders")
@Getter @Setter
public class GenderEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false, length = 75)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "gender")
    private List<UserEntity> users = new ArrayList<>();
}
