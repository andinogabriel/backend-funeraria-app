package disenodesistemas.backendfunerariaapp.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
public class ErrorMessage {

    private LocalDateTime timestamp;
    private String message;

    public ErrorMessage(String message) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }

}
