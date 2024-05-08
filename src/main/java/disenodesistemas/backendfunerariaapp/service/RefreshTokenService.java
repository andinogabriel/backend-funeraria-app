package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.dto.request.TokenRefreshRequestDto;
import disenodesistemas.backendfunerariaapp.entities.RefreshToken;

public interface RefreshTokenService {
  RefreshToken findByToken(String token);

  RefreshToken save(RefreshToken refreshToken);

  RefreshToken createRefreshToken();

  void verifyExpiration(RefreshToken token);

  void delete(RefreshToken refreshToken);

  void increaseCount(RefreshToken refreshToken);

  JwtDto refreshJwtToken(TokenRefreshRequestDto tokenRefreshRequestDto);
}
