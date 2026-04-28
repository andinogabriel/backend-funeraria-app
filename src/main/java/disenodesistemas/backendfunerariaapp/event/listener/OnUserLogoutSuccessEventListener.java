package disenodesistemas.backendfunerariaapp.event.listener;

import disenodesistemas.backendfunerariaapp.infrastructure.security.jwt.LoggedOutJwtTokenCache;
import disenodesistemas.backendfunerariaapp.event.OnUserLogoutSuccessEvent;
import disenodesistemas.backendfunerariaapp.web.dto.DeviceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Reacts to successful logout events and propagates the technical side effects required by the
 * security layer. Its main responsibility is to register the logged-out token in the short-lived
 * cache so subsequent requests can reject it before the JWT naturally expires.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OnUserLogoutSuccessEventListener {

  private final LoggedOutJwtTokenCache tokenCache;

  /**
   * Handles the logout-success domain event emitted by the session use case. The listener records
   * operational context in the logs and then stores the logout marker so the access token can be
   * rejected immediately by the JWT validation layer.
   */
  @EventListener
  public void onApplicationEvent(final OnUserLogoutSuccessEvent event) {
    if (event == null) {
      return;
    }

    final DeviceInfo deviceInfo = event.logOutRequest() == null ? null : event.logOutRequest().deviceInfo();
    log.atInfo()
        .addKeyValue("event", "user.logout.completed")
        .addKeyValue("email", event.userEmail())
        .addKeyValue("deviceId", deviceInfo == null ? null : deviceInfo.deviceId())
        .addKeyValue("deviceType", deviceInfo == null ? null : deviceInfo.deviceType())
        .log("user.logout.completed");
    tokenCache.markLogoutEventForToken(event);
  }
}
