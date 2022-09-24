package disenodesistemas.backendfunerariaapp.utils;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
@Value
public class OperationStatusModel {
    String name;
    String result;
}
