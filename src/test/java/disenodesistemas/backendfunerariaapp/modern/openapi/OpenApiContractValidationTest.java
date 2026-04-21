package disenodesistemas.backendfunerariaapp.modern.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class OpenApiContractValidationTest {

  private static final Path OPEN_API_PATH =
      Path.of("src", "main", "resources", "openapi", "openapi.yaml");

  @Test
  @DisplayName(
      "given the published OpenAPI contract when local references are resolved then every pointer must exist")
  void shouldResolveAllLocalReferences() throws IOException {
    final Map<String, Object> document = loadDocument();
    final List<String> references = new ArrayList<>();
    collectReferences(document, references);

    final List<String> unresolvedReferences =
        references.stream().filter(reference -> !pointerExists(document, reference)).toList();

    assertThat(unresolvedReferences).isEmpty();
  }

  @Test
  @DisplayName(
      "given the published OpenAPI contract when it is validated then core sections must stay documented")
  void shouldDocumentCoreContractSections() throws IOException {
    final Map<String, Object> document = loadDocument();

    assertThat(pointerExists(document, "#/openapi")).isTrue();
    assertThat(pointerExists(document, "#/paths")).isTrue();
    assertThat(pointerExists(document, "#/components/schemas/ProblemDetail")).isTrue();
    assertThat(pointerExists(document, "#/components/securitySchemes/bearerAuth")).isTrue();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> loadDocument() throws IOException {
    final Yaml yaml = new Yaml();

    try (InputStream inputStream = Files.newInputStream(OPEN_API_PATH)) {
      return yaml.load(inputStream);
    }
  }

  @SuppressWarnings("unchecked")
  private void collectReferences(final Object node, final List<String> references) {
    if (node instanceof Map<?, ?> mapNode) {
      mapNode.forEach(
          (key, value) -> {
            if ("$ref".equals(key) && value instanceof String reference) {
              references.add(reference);
            } else {
              collectReferences(value, references);
            }
          });
      return;
    }

    if (node instanceof List<?> listNode) {
      listNode.forEach(item -> collectReferences(item, references));
    }
  }

  private boolean pointerExists(final Map<String, Object> document, final String reference) {
    if (!reference.startsWith("#/")) {
      return true;
    }

    Object currentNode = document;
    for (String token : reference.substring(2).split("/")) {
      currentNode = resolveToken(currentNode, decodePointerToken(token));
      if (currentNode == null) {
        return false;
      }
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  private Object resolveToken(final Object currentNode, final String token) {
    if (currentNode instanceof Map<?, ?> mapNode) {
      return ((Map<String, Object>) mapNode).get(token);
    }

    if (currentNode instanceof List<?> listNode && token.chars().allMatch(Character::isDigit)) {
      final int index = Integer.parseInt(token);
      return index < listNode.size() ? listNode.get(index) : null;
    }

    return null;
  }

  private String decodePointerToken(final String token) {
    return token.replace("~1", "/").replace("~0", "~");
  }
}
