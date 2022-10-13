package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class ItemPlanIdDto {
    Long planId;
    Long itemId;
}
