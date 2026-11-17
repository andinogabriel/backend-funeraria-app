package disenodesistemas.backendfunerariaapp.modern.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Keeps every third-party GitHub Action pinned to a full commit SHA. A tag such as {@code @v4} can
 * be moved by whoever controls the action's repository, and the release workflow runs with
 * {@code packages: write} and an OIDC token that signs images. A SHA cannot be moved. The trailing
 * {@code # vX.Y.Z} comment keeps the version readable and lets Dependabot propose upgrades.
 */
@DisplayName("GitHub Actions pinning")
class WorkflowActionPinningGuardrailsTest {

  /** Relative to the module root, which is Maven's working directory for tests. */
  private static final Path WORKFLOWS = Path.of(".github", "workflows");

  /** Sanity floor: fewer matches means the parsing broke, not that the workflows got smaller. */
  private static final int MIN_EXPECTED_USES = 10;

  private static final Pattern USES =
      Pattern.compile("^\\s*(?:- )?uses:\\s*(?<ref>\\S+)(?<rest>.*)$");

  private static final Pattern PINNED = Pattern.compile("^[\\w.-]+/[\\w./-]+@[0-9a-f]{40}$");

  private static final Pattern VERSION_COMMENT = Pattern.compile("^\\s+# v\\d+(\\.\\d+)*\\s*$");

  @Test
  @DisplayName("Given the workflows when their actions are read then each one is pinned to a commit SHA")
  void everyActionIsPinnedToACommitSha() throws IOException {
    assertThat(WORKFLOWS).as("run from the module root").isDirectory();
    final List<String> uses;
    try (Stream<Path> files = Files.list(WORKFLOWS)) {
      uses =
          files
              .filter(
                  (final Path path) ->
                      path.toString().endsWith(".yml") || path.toString().endsWith(".yaml"))
              .flatMap(WorkflowActionPinningGuardrailsTest::usesLines)
              .toList();
    }

    assertThat(uses)
        .as("uses: entries found under .github/workflows")
        .hasSizeGreaterThan(MIN_EXPECTED_USES);
    assertThat(uses)
        .allSatisfy(
            (final String line) -> {
              final Matcher matcher = USES.matcher(line.substring(line.indexOf(": ") + 2));
              assertThat(matcher.matches()).as(line).isTrue();
              assertThat(matcher.group("ref"))
                  .as("%s must pin owner/repo@<40-char commit SHA>", line)
                  .matches(PINNED);
              assertThat(matcher.group("rest"))
                  .as("%s must end with a '# vX.Y.Z' comment", line)
                  .matches(VERSION_COMMENT);
            });
  }

  /** Every {@code uses:} line of one workflow, prefixed with {@code file:line: } for the report. */
  private static Stream<String> usesLines(final Path file) {
    try {
      final List<String> lines = Files.readAllLines(file);
      return IntStream.range(0, lines.size())
          .filter((final int index) -> USES.matcher(lines.get(index)).matches())
          .filter((final int index) -> !lines.get(index).contains("uses: ./"))
          .mapToObj(
              (final int index) -> file.getFileName() + ":" + (index + 1) + ": " + lines.get(index));
    } catch (final IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }
}
