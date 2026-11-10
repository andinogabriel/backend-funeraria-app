package disenodesistemas.backendfunerariaapp.modern.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Keeps docker-compose from publishing anything but the API on every host interface. Postgres
 * (default credentials in development) and the observability UIs (no login) used to listen on
 * 0.0.0.0, reachable from the whole LAN. Each published port must now name a loopback address
 * or a {@code *_BIND_ADDRESS} variable that defaults to loopback.
 */
@DisplayName("docker-compose port binding")
class ComposePortBindingGuardrailsTest {

  /** A short-syntax port entry: {@code - "host:container"} with an optional bind address. */
  private static final Pattern PORT_ENTRY = Pattern.compile("^\\s+- \"(?<mapping>[^\"]+:\\d+)\"\\s*$");

  private static final Pattern LOOPBACK_BINDING =
      Pattern.compile("^(127\\.0\\.0\\.1|\\$\\{[A-Z_]+_BIND_ADDRESS:-127\\.0\\.0\\.1\\}):.+");

  /** The API is the one service meant to be reached from other machines. */
  private static final String PUBLIC_API = "${SERVER_PORT:-8081}:8081";

  @Test
  @DisplayName("Given the compose file when its published ports are read then only the API listens beyond loopback")
  void onlyTheApiIsPublishedOnEveryInterface() throws IOException {
    final List<String> mappings =
        Files.readAllLines(Path.of("docker-compose.yml")).stream()
            .map(PORT_ENTRY::matcher)
            .filter(java.util.regex.Matcher::matches)
            .map(matcher -> matcher.group("mapping"))
            .toList();

    assertThat(mappings).as("published ports found in docker-compose.yml").hasSizeGreaterThan(5);
    assertThat(mappings)
        .filteredOn(mapping -> !mapping.equals(PUBLIC_API))
        .allSatisfy(
            mapping ->
                assertThat(mapping)
                    .as("port %s must bind 127.0.0.1 or a *_BIND_ADDRESS defaulting to it", mapping)
                    .matches(LOOPBACK_BINDING));
  }
}
