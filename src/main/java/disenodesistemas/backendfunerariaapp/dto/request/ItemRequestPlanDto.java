package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class ItemRequestPlanDto {
    Long id;
    String name;
    String code;
}
