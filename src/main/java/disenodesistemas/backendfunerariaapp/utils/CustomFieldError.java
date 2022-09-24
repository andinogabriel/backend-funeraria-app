package disenodesistemas.backendfunerariaapp.utils;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class CustomFieldError {
    String field;
    String message;
}
