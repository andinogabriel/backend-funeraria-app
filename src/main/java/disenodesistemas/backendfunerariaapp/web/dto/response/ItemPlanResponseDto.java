package disenodesistemas.backendfunerariaapp.web.dto.response;

public record ItemPlanResponseDto(
    ItemResponseDto item,
    Integer quantity
) {}