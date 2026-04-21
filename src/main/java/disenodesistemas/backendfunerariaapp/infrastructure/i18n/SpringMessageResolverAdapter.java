package disenodesistemas.backendfunerariaapp.infrastructure.i18n;

import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringMessageResolverAdapter implements MessageResolverPort {

  private final MessageSource messageSource;

  @Override
  public String getMessage(final String code, final Object... args) {
    return messageSource.getMessage(code, args, code, LocaleContextHolder.getLocale());
  }
}
