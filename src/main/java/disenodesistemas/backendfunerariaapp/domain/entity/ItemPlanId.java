package disenodesistemas.backendfunerariaapp.domain.entity;

import java.io.Serializable;
import jakarta.persistence.Embeddable;
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
  private Long planId;
  private Long itemId;
}
