package disenodesistemas.backendfunerariaapp.utils;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class ValidationErrors {
    Map<String, String> errors;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="dd-MM-yyyy HH:mm")
    LocalDateTime timestamp;
    String id;
    HttpStatus status;
    int code;
}
