package disenodesistemas.backendfunerariaapp.entities;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemPlanEntityMother {

    public static ItemPlanEntity getItemPlanEntity() {
        return ItemPlanEntity.builder()
                .id(new ItemPlanId(1L, 1L))
                .item(ItemEntityMother.getItem())
                .plan(PlanEntityMother.getPlan())
                .quantity(2)
                .build();
    }


}