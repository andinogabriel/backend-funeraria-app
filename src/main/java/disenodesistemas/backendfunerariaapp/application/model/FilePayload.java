package disenodesistemas.backendfunerariaapp.application.model;

public record FilePayload(String originalFilename, String contentType, byte[] content) {

  public long size() {
    return content == null ? 0L : content.length;
  }
}
