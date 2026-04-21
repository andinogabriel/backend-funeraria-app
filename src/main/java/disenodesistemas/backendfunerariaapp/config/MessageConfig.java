package disenodesistemas.backendfunerariaapp.config;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
public class MessageConfig {

  @Bean("messageSource")
  public MessageSource messageSource() {
    final ReloadableResourceBundleMessageSource messageSource =
        new ReloadableResourceBundleMessageSource();
    messageSource.setBasenames("classpath:messages/messages", "classpath:messages");
    messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
    messageSource.setFallbackToSystemLocale(false);
    messageSource.setUseCodeAsDefaultMessage(true);
    return messageSource;
  }

  @Bean
  public LocalValidatorFactoryBean validator(final MessageSource messageSource) {
    final LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
    bean.setValidationMessageSource(messageSource);
    return bean;
  }

  @Bean
  public LocaleResolver localeResolver() {
    final AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
    localeResolver.setDefaultLocale(Locale.of("es", "AR"));
    return localeResolver;
  }
}
