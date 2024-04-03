package disenodesistemas.backendfunerariaapp.event;

import disenodesistemas.backendfunerariaapp.dto.request.LogOutRequestDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
public class OnUserLogoutSuccessEvent extends ApplicationEvent {
  private static final long serialVersionUID = 1L;
  private final String userEmail;
  private final String token;
  private final transient LogOutRequestDto logOutRequest;
  private final Date eventTime;

  public OnUserLogoutSuccessEvent(
      final String userEmail, final String token, final LogOutRequestDto logOutRequest) {
    super(userEmail);
    this.userEmail = userEmail;
    this.token = token;
    this.logOutRequest = logOutRequest;
    this.eventTime = Date.from(Instant.now());
  }
}
