package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.web.dto.JwtDto;
import java.util.Optional;

public interface AuthIdempotencyPort {

  Optional<JwtDto> findJwtResponse(String operation, String idempotencyKey, String requestFingerprint);

  void storeJwtResponse(
      String operation, String idempotencyKey, String requestFingerprint, JwtDto response);
}
