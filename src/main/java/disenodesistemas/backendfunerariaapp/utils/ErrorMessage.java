package disenodesistemas.backendfunerariaapp.utils;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class ErrorMessage {
    @JsonFormat(pattern="dd-MM-yyyy HH:mm")
    LocalDateTime timestamp;

    String message;
}
