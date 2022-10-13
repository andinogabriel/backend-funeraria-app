package disenodesistemas.backendfunerariaapp.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ItemPlanId implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long planId;
    private Long itemId;
}
