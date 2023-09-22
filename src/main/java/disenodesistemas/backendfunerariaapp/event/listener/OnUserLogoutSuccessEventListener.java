package disenodesistemas.backendfunerariaapp.event.listener;

import disenodesistemas.backendfunerariaapp.cache.LoggedOutJwtTokenCache;
import disenodesistemas.backendfunerariaapp.dto.DeviceInfo;
import disenodesistemas.backendfunerariaapp.event.OnUserLogoutSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class OnUserLogoutSuccessEventListener implements ApplicationListener<OnUserLogoutSuccessEvent> {

    private final LoggedOutJwtTokenCache tokenCache;

    public void onApplicationEvent(final OnUserLogoutSuccessEvent event) {
        if (!Objects.equals(event, null)) {
            final DeviceInfo deviceInfo = event.getLogOutRequest().getDeviceInfo();
            log.info(String.format("Log out success event received for user [%s] for device [%s]", event.getUserEmail(), deviceInfo));
            tokenCache.markLogoutEventForToken(event);
        }
    }
}
