package disenodesistemas.backendfunerariaapp.modern.web.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserAccountUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserActiveSessionsUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserProfileUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserRoleUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionUseCase;
import disenodesistemas.backendfunerariaapp.exception.AppExceptionsHandler;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.RefreshTokenSecurityProperties;
import disenodesistemas.backendfunerariaapp.web.controller.UserController;
import disenodesistemas.backendfunerariaapp.web.dto.UserModelAssembler;
import disenodesistemas.backendfunerariaapp.web.dto.response.UserSessionResponseDto;
import disenodesistemas.backendfunerariaapp.web.support.RefreshTokenCookies;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@DisplayName("UserController active sessions")
class UserControllerActiveSessionsTest {

  private final UserActiveSessionsUseCase useCase = mock(UserActiveSessionsUseCase.class);
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    final UserController controller =
        new UserController(
            mock(UserAccountUseCase.class),
            mock(UserSessionUseCase.class),
            mock(UserProfileUseCase.class),
            mock(UserRoleUseCase.class),
            mock(UserQueryUseCase.class),
            mock(UserModelAssembler.class),
            mock(AuthenticatedUserPort.class),
            new RefreshTokenCookies(new RefreshTokenSecurityProperties(604800, 32, 30, true)),
            useCase);
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new AppExceptionsHandler(mock(MessageResolverPort.class)))
            .build();
  }

  @Test
  @DisplayName("Given two sessions when they are listed then both come back with the current flag")
  void listsTheSessions() throws Exception {
    final Instant now = Instant.parse("2026-11-17T12:00:00Z");
    when(useCase.listMine())
        .thenReturn(
            List.of(
                new UserSessionResponseDto(10L, "BROWSER", "Firefox", "10.0.0.1", now, now, true),
                new UserSessionResponseDto(11L, "BROWSER", "Chrome", "10.0.0.2", now, now, false)));

    mockMvc
        .perform(get("/api/v1/users/me/sessions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(10))
        .andExpect(jsonPath("$[0].current").value(true))
        .andExpect(jsonPath("$[1].userAgent").value("Chrome"));
  }

  @Test
  @DisplayName("Given another session when it is closed then the answer is 204")
  void closesOneSession() throws Exception {
    mockMvc.perform(delete("/api/v1/users/me/sessions/11")).andExpect(status().isNoContent());

    verify(useCase).closeMine(11L);
  }

  @Test
  @DisplayName("Given the current session when it is closed then the answer is 409")
  void refusesTheCurrentSession() throws Exception {
    doThrow(new ConflictException("user.session.error.current")).when(useCase).closeMine(10L);

    mockMvc.perform(delete("/api/v1/users/me/sessions/10")).andExpect(status().isConflict());
  }

  @Test
  @DisplayName("Given a session of another user when it is closed then the answer is 404")
  void anotherUsersSessionIsNotFound() throws Exception {
    doThrow(new NotFoundException("user.session.error.not.found")).when(useCase).closeMine(99L);

    mockMvc.perform(delete("/api/v1/users/me/sessions/99")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Given other sessions when they are all closed then the count is returned")
  void closesTheOthers() throws Exception {
    when(useCase.closeOthers()).thenReturn(3);

    mockMvc
        .perform(delete("/api/v1/users/me/sessions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.closed").value(3));
  }
}
