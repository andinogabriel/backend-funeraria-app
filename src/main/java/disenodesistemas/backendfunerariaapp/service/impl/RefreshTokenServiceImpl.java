package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.dto.request.TokenRefreshRequestDto;
import disenodesistemas.backendfunerariaapp.entities.RefreshToken;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.RefreshTokenRepository;
import disenodesistemas.backendfunerariaapp.security.SecurityConstants;
import disenodesistemas.backendfunerariaapp.security.jwt.JwtProvider;
import disenodesistemas.backendfunerariaapp.service.RefreshTokenService;
import disenodesistemas.backendfunerariaapp.service.UserDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.HttpStatus.EXPECTATION_FAILED;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDeviceService userDeviceService;
    private final JwtProvider jwtProvider;

    @Override
    public RefreshToken findByToken(final String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("refresh.token.error.not.found"));
    }

    @Override
    public RefreshToken save(final RefreshToken refreshToken) {
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken createRefreshToken() {
        final RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiryDate(Instant.now().plusMillis(3600000));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setRefreshCount(0L);
        return refreshToken;
    }

    @Override
    public void verifyExpiration(final RefreshToken token) {
        if ((token.getExpiryDate().isBefore(Instant.now())))
            throw new AppException("refresh.token.error.expired", EXPECTATION_FAILED);
    }

    @Override
    public void deleteById(final Long id) {
        refreshTokenRepository.deleteById(id);
    }

    @Override
    public void increaseCount(final RefreshToken refreshToken) {
        refreshToken.setRefreshCount(refreshToken.getRefreshCount() + 1);
        save(refreshToken);
    }

    @Override
    public JwtDto refreshJwtToken(final TokenRefreshRequestDto tokenRefreshRequestDto) {
        final RefreshToken refreshToken = findByToken(tokenRefreshRequestDto.getRefreshToken());

        verifyExpiration(refreshToken);
        userDeviceService.verifyRefreshAvailability(refreshToken);

        increaseCount(refreshToken);
        final RefreshToken refreshTokenSaved = save(refreshToken);

        final UserEntity userEntity = refreshTokenSaved.getUserDevice().getUser();
        final String generatedToken = jwtProvider.generateTokenFromUser(userEntity);

        return JwtDto.builder()
                .authorization(SecurityConstants.TOKEN_PREFIX + generatedToken)
                .refreshToken(refreshToken.getToken())
                .authorities(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .expiryDuration(jwtProvider.getExpiryDuration())
                .build();
    }
}
