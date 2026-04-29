package disenodesistemas.backendfunerariaapp.infrastructure.security;

import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import disenodesistemas.backendfunerariaapp.application.port.out.DeviceFingerprintPort;
import disenodesistemas.backendfunerariaapp.application.port.out.SecurityThreatProtectionPort;
import disenodesistemas.backendfunerariaapp.application.port.out.UserDevicePort;
import disenodesistemas.backendfunerariaapp.infrastructure.logging.RequestTracingFilter;
import disenodesistemas.backendfunerariaapp.infrastructure.security.jwt.JwtEntryPoint;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.JwtProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.security.jwt.JwtProvider;
import disenodesistemas.backendfunerariaapp.infrastructure.security.jwt.JwtTokenFilter;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.AuthIdempotencyProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.PasswordSecurityProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.security.PepperedPasswordEncoder;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.RefreshTokenSecurityProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.SecurityRequestProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.LoginRateLimitProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.ThreatProtectionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
@EnableConfigurationProperties({
  JwtProperties.class,
  PasswordSecurityProperties.class,
  LoginRateLimitProperties.class,
  SecurityRequestProperties.class,
  RefreshTokenSecurityProperties.class,
  ThreatProtectionProperties.class,
  AuthIdempotencyProperties.class
})
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtEntryPoint jwtEntryPoint;
  private final JwtProvider jwtProvider;
  private final JwtProperties jwtProperties;
  private final PasswordSecurityProperties passwordSecurityProperties;
  private final MessageResolverPort messageResolverPort;
  private final DeviceFingerprintPort deviceFingerprintPort;
  private final SecurityThreatProtectionPort securityThreatProtectionPort;
  private final UserDevicePort userDevicePort;
  private final SecurityRequestProperties securityRequestProperties;
  private final RequestTracingFilter requestTracingFilter;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new PepperedPasswordEncoder(
        new Argon2PasswordEncoder(
            passwordSecurityProperties.saltLength(),
            passwordSecurityProperties.hashLength(),
            passwordSecurityProperties.parallelism(),
            passwordSecurityProperties.memoryKb(),
            passwordSecurityProperties.iterations()),
        passwordSecurityProperties.pepper());
  }

  @Bean
  public JwtTokenFilter jwtTokenFilter() {
    return new JwtTokenFilter(
        jwtProperties,
        jwtProvider,
        messageResolverPort,
        deviceFingerprintPort,
        securityThreatProtectionPort,
        userDevicePort,
        securityRequestProperties);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      final HttpSecurity http,
      final JwtTokenFilter jwtTokenFilter,
      final ObjectProvider<CorsConfigurationSource> corsConfigurationSourceProvider)
      throws Exception {
    final CorsConfigurationSource corsConfigurationSource =
        corsConfigurationSourceProvider.getIfAvailable();
    if (corsConfigurationSource != null) {
      http.cors(cors -> cors.configurationSource(corsConfigurationSource));
    }

    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtEntryPoint))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                HttpMethod.POST,
                "/api/v1/users",
                "/api/v1/users/login",
                "/api/v1/users/refresh",
                "/api/v1/users/forgot-password",
                "/api/v1/addresses"
            ).permitAll()
            .requestMatchers(
                HttpMethod.GET,
                "/actuator/health",
                "/actuator/health/**",
                "/actuator/info",
                "/actuator/prometheus",
                "/api/v1/users/activation",
                "/api/v1/categories",
                "/api/v1/provinces",
                "/api/v1/cities"
            ).permitAll()
            .anyRequest().authenticated()
        );

    http.addFilterBefore(requestTracingFilter, SecurityContextHolderFilter.class);
    http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
