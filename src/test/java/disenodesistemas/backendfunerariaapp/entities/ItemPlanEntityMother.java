package disenodesistemas.backendfunerariaapp.entities;

import disenodesistemas.backendfunerariaapp.dto.request.ItemPlanRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.ItemRequestPlanDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemPlanEntityMother {

  private static final String NAME = "Corona simple";
  private static final String CODE = "67ad6c26-f586-4cb2-9d5e-3fbcc3e2e8eb";

  public static ItemPlanEntity getItemPlanEntity() {
    return ItemPlanEntity.builder()
        .id(new ItemPlanId(1L, 1L))
        .item(ItemEntityMother.getItem())
        .plan(PlanEntityMother.getPlan())
        .quantity(2)
        .build();
  }

  public static ItemPlanRequestDto getItemPlanRequest() {
    return ItemPlanRequestDto.builder()
        .item(ItemRequestPlanDto.builder().id(1L).name(NAME).code(CODE).build())
        .quantity(2)
        .build();
  }

  public static ItemPlanRequestDto getItemPlanRequestInvalidCode() {
    return ItemPlanRequestDto.builder()
        .item(ItemRequestPlanDto.builder().id(1L).name(NAME).code("asd-123").build())
        .quantity(2)
        .build();
  }

  public static ItemPlanRequestDto getItemPlanRequestItemWithoutPrice() {
    return ItemPlanRequestDto.builder()
        .item(
            ItemRequestPlanDto.builder()
                .id(3L)
                .name(NAME)
                .code("01c5fc8a-9ab2-4c85-ac15-6b66b92b266c")
                .build())
        .quantity(2)
        .build();
  }
}
