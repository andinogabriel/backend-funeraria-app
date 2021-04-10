package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "relationships") //Parentesco
@Getter @Setter
public class RelationshipEntity implements Serializable {
    //Family or another relationship with the user
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false, length = 60)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "affiliateRelationship")
    private List<AffiliateEntity> affiliates = new ArrayList<>();

}
