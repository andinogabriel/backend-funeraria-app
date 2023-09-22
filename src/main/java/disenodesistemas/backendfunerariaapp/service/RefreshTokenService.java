package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.dto.request.TokenRefreshRequestDto;
import disenodesistemas.backendfunerariaapp.entities.RefreshToken;

public interface RefreshTokenService {
    RefreshToken findByToken(String token);
    RefreshToken save(RefreshToken refreshToken);
    RefreshToken createRefreshToken();
    void verifyExpiration(RefreshToken token);
    void deleteById(Long id);
    void increaseCount(RefreshToken refreshToken);
    JwtDto refreshJwtToken(TokenRefreshRequestDto tokenRefreshRequestDto);
}
