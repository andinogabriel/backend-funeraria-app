package disenodesistemas.backendfunerariaapp.entities;

import java.io.Serializable;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
