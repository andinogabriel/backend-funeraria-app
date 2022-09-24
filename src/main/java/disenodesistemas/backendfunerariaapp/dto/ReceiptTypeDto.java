package disenodesistemas.backendfunerariaapp.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class ReceiptTypeDto  {
    Long id;
    String name;
}
