package disenodesistemas.backendfunerariaapp.service.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.dto.request.TokenRefreshRequestDto;
import disenodesistemas.backendfunerariaapp.entities.RefreshToken;
import disenodesistemas.backendfunerariaapp.entities.UserDevice;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.RefreshTokenRepository;
import disenodesistemas.backendfunerariaapp.security.SecurityConstants;
import disenodesistemas.backendfunerariaapp.security.jwt.JwtProvider;
import disenodesistemas.backendfunerariaapp.service.UserDeviceService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private UserDeviceService userDeviceService;
  @Mock private JwtProvider jwtProvider;
  @InjectMocks private RefreshTokenServiceImpl sut;
  private static String token;

  @BeforeEach
  void setUp() {
    token = UUID.randomUUID().toString();
  }

  @Test
  void findByToken() {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setToken(token);
    given(refreshTokenRepository.findByToken(token)).willReturn(Optional.of(refreshToken));

    final RefreshToken foundToken = sut.findByToken(token);

    assertAll(() -> assertNotNull(foundToken), () -> assertEquals(token, foundToken.getToken()));
    then(refreshTokenRepository).should(times(1)).findByToken(token);
  }

  @Test
  void findByToken_shouldThrowNotFoundException_whenTokenDoesNotExist() {
    given(refreshTokenRepository.findByToken(token))
        .willThrow(new NotFoundException("refresh.token.error.not.found"));

    final NotFoundException exception =
        assertThrows(NotFoundException.class, () -> sut.findByToken(token));

    assertAll(
        () -> assertEquals(HttpStatus.NOT_FOUND, exception.getStatus()),
        () -> assertEquals("refresh.token.error.not.found", exception.getMessage()));
    then(refreshTokenRepository).should(times(1)).findByToken(token);
  }

  @Test
  void save() {
    RefreshToken refreshToken = new RefreshToken();
    given(refreshTokenRepository.save(refreshToken)).willReturn(refreshToken);

    final RefreshToken savedToken = sut.save(refreshToken);

    assertNotNull(savedToken);
    then(refreshTokenRepository).should(times(1)).save(refreshToken);
  }

  @Test
  void createRefreshToken() {
    final Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            "email@gmail.com", "asd123asd", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    SecurityContextHolder.setContext(securityContext);
    securityContext.setAuthentication(authentication);

    final String refreshJwtToken =
        "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJhdXRob3JpdGllcyI6IlJPTEVfQURNSU4sUk9MRV9VU0VSIiwiaWF0IjoxNzE1MTI3ODMyLCJleHAiOjE4MDE1Mjc4MzJ9.wFaYSqujYe4xgKmIcJZQEMFR3rrw39Raw6o7KXIRyJM0xFplOEsGCygRQRD-Tglr641q0bcTVDKYQ7Ky-NG7rA";
    given(jwtProvider.generateToken(authentication)).willReturn(refreshJwtToken);

    final RefreshToken refreshToken = sut.createRefreshToken();

    assertAll(
        () -> assertNotNull(refreshToken),
        () -> assertNotNull(refreshToken.getToken()),
        () -> assertEquals(0L, refreshToken.getRefreshCount()),
        () -> assertTrue(refreshToken.getExpiryDate().isAfter(Instant.now())));
  }

  @Test
  void verifyExpiration_shouldThrowAppException_whenTokenIsExpired() {
    final RefreshToken refreshToken = new RefreshToken();
    refreshToken.setExpiryDate(Instant.now().minusMillis(3600000));

    final AppException exception =
        assertThrows(AppException.class, () -> sut.verifyExpiration(refreshToken));

    assertAll(
        () -> assertEquals(HttpStatus.EXPECTATION_FAILED, exception.getStatus()),
        () -> assertEquals("refresh.token.error.expired", exception.getMessage()));
  }

  @Test
  void verifyExpiration_shouldNotThrowException_whenTokenIsNotExpired() {
    final RefreshToken refreshToken = new RefreshToken();
    refreshToken.setExpiryDate(Instant.now().plusMillis(3600000));
    assertDoesNotThrow(() -> sut.verifyExpiration(refreshToken));
  }

  @Test
  void delete() {
    final RefreshToken refreshToken = new RefreshToken();
    willDoNothing().given(refreshTokenRepository).delete(refreshToken);

    sut.delete(refreshToken);

    then(refreshTokenRepository).should(times(1)).delete(refreshToken);
  }

  @Test
  void increaseCount() {
    final RefreshToken refreshToken = new RefreshToken();
    refreshToken.setRefreshCount(0L);
    given(refreshTokenRepository.save(refreshToken)).willReturn(refreshToken);

    sut.increaseCount(refreshToken);

    assertEquals(1L, refreshToken.getRefreshCount());
    then(refreshTokenRepository).should(times(1)).save(refreshToken);
  }

  @Test
  void refreshJwtToken() {
    setAuthenticationContext();
    final String refreshTokenString = UUID.randomUUID().toString();
    final TokenRefreshRequestDto tokenRefreshRequestDto =
        TokenRefreshRequestDto.builder().refreshToken(refreshTokenString).build();
    val refreshToken = new RefreshToken();
    val userDevice = new UserDevice();
    val userEntity = new UserEntity();
    val newJwtToken = "new.jwt.token";
    refreshToken.setToken(refreshTokenString);
    refreshToken.setExpiryDate(Instant.now().plusMillis(3600000));
    refreshToken.setRefreshCount(0L);
    userDevice.setUser(userEntity);
    refreshToken.setUserDevice(userDevice);

    given(refreshTokenRepository.findByToken(refreshTokenString))
        .willReturn(Optional.of(refreshToken));
    given(jwtProvider.generateTokenFromUser(userEntity)).willReturn(newJwtToken);
    given(refreshTokenRepository.save(any(RefreshToken.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    final JwtDto jwtDto = sut.refreshJwtToken(tokenRefreshRequestDto);

    assertAll(
        () -> assertNotNull(jwtDto),
        () -> assertEquals(SecurityConstants.TOKEN_PREFIX + newJwtToken, jwtDto.getAuthorization()),
        () -> assertEquals(refreshTokenString, jwtDto.getRefreshToken()),
        () -> assertEquals(1L, refreshToken.getRefreshCount()));
    then(refreshTokenRepository).should(times(1)).findByToken(refreshTokenString);
    then(userDeviceService).should(times(1)).verifyRefreshAvailability(refreshToken);
    then(refreshTokenRepository).should(times(2)).save(refreshToken);
    then(jwtProvider).should(times(1)).generateTokenFromUser(userEntity);
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticationContext() {
    final List<GrantedAuthority> grantedAuthorities =
        List.of(new SimpleGrantedAuthority("ROLE_USER"));
    final Authentication authentication =
        new UsernamePasswordAuthenticationToken("user", "password", grantedAuthorities);
    final SecurityContext securityContext = mock(SecurityContext.class);
    given(securityContext.getAuthentication()).willReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
  }
}
