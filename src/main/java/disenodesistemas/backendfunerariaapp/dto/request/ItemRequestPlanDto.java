package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder(toBuilder = true)
public class ItemRequestPlanDto {
    private final Long id;
    private final String name;
    private final String code;
}
