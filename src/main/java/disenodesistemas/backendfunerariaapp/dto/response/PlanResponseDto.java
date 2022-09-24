package disenodesistemas.backendfunerariaapp.dto.response;

import java.math.BigDecimal;
import java.util.Set;

public interface PlanResponseDto {

    Long getId();
    String getName();
    String getDescription();
    String getImageUrl();
    BigDecimal getPrice();
    Set<ItemPlanEntity> getItemsPlan();

    interface ItemPlanEntity {
        ItemResponseDto getItem();
        Integer getQuantity();
    }

}
