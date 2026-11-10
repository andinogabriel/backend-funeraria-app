package disenodesistemas.backendfunerariaapp.modern.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserAccountUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserProfileUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserRoleUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionUseCase;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.exception.AppExceptionsHandler;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.RefreshTokenSecurityProperties;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.controller.UserController;
import disenodesistemas.backendfunerariaapp.web.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.web.dto.UserModelAssembler;
import disenodesistemas.backendfunerariaapp.web.support.RefreshTokenCookies;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@DisplayName("UserController session cookie")
class UserControllerSessionCookieTest {

  private static final String DEVICE = "{\"deviceInfo\":{\"deviceId\":\"d-1\",\"deviceType\":\"BROWSER\"}}";

  private final UserSessionUseCase userSessionUseCase = mock(UserSessionUseCase.class);
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    final UserController controller =
        new UserController(
            mock(UserAccountUseCase.class),
            userSessionUseCase,
            mock(UserProfileUseCase.class),
            mock(UserRoleUseCase.class),
            mock(UserQueryUseCase.class),
            mock(UserModelAssembler.class),
            mock(AuthenticatedUserPort.class),
            new RefreshTokenCookies(new RefreshTokenSecurityProperties(604800, 32, 30, true)));
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new AppExceptionsHandler(mock(MessageResolverPort.class)))
            .build();
  }

  @Test
  @DisplayName(
      "Given a successful login when the response is written then the refresh token is only in an HttpOnly, Secure, SameSite=Strict cookie")
  void loginMovesTheRefreshTokenIntoTheCookie() throws Exception {
    when(userSessionUseCase.login(any())).thenReturn(session("issued-refresh"));

    final MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"email\":\"a@b.com\",\"password\":\"x\","
                            + DEVICE.substring(1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authorization").value("Bearer access"))
            .andExpect(jsonPath("$.refreshToken").doesNotExist())
            .andReturn();

    assertThat(result.getResponse().getContentAsString()).doesNotContain("issued-refresh");
    assertThat(result.getResponse().getHeader(HttpHeaders.SET_COOKIE))
        .startsWith("refresh_token=issued-refresh")
        .contains("Path=/api/v1/users", "Max-Age=604800", "Secure", "HttpOnly", "SameSite=Strict");
  }

  @Test
  @DisplayName(
      "Given a refresh cookie when refresh is requested then the use case receives the cookie value and the rotated token replaces it")
  void refreshReadsAndRotatesTheCookie() throws Exception {
    when(userSessionUseCase.refreshJwtToken(eq("current-refresh"), any()))
        .thenReturn(session("rotated-refresh"));

    final MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/users/refresh")
                    .cookie(new Cookie("refresh_token", "current-refresh"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(DEVICE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.refreshToken").doesNotExist())
            .andReturn();

    assertThat(result.getResponse().getHeader(HttpHeaders.SET_COOKIE))
        .startsWith("refresh_token=rotated-refresh");
  }

  @Test
  @DisplayName(
      "Given no refresh cookie when refresh is requested then the use case receives null and decides the outcome")
  void refreshWithoutCookiePassesNull() throws Exception {
    when(userSessionUseCase.refreshJwtToken(isNull(), any())).thenReturn(session("x"));

    mockMvc
        .perform(
            post("/api/v1/users/refresh").contentType(MediaType.APPLICATION_JSON).content(DEVICE))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Given a logout when the response is written then the refresh cookie is expired")
  void logoutClearsTheCookie() throws Exception {
    when(userSessionUseCase.logoutUser(any()))
        .thenReturn(new OperationStatusModel("SUCCESS", "ok"));

    final MvcResult result =
        mockMvc
            .perform(
                put("/api/v1/users/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"token\":\"access\"," + DEVICE.substring(1)))
            .andExpect(status().isOk())
            .andReturn();

    assertThat(result.getResponse().getHeader(HttpHeaders.SET_COOKIE))
        .startsWith("refresh_token=;")
        .contains("Max-Age=0", "Path=/api/v1/users", "HttpOnly");
  }

  @Test
  @DisplayName(
      "Given a logout the backend rejects when the response is written then the refresh cookie is still expired")
  void failedLogoutStillClearsTheCookie() throws Exception {
    when(userSessionUseCase.logoutUser(any()))
        .thenThrow(new AppException("security.jwt.session.inactive", HttpStatus.UNAUTHORIZED));

    final MvcResult result =
        mockMvc
            .perform(
                put("/api/v1/users/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"token\":\"access\"," + DEVICE.substring(1)))
            .andExpect(status().isUnauthorized())
            .andReturn();

    assertThat(result.getResponse().getHeader(HttpHeaders.SET_COOKIE))
        .startsWith("refresh_token=;")
        .contains("Max-Age=0");
  }

  private static JwtDto session(final String refreshToken) {
    return JwtDto.builder()
        .authorization("Bearer access")
        .refreshToken(refreshToken)
        .expiryDuration(900_000L)
        .authorities(List.of("ROLE_USER"))
        .build();
  }
}
