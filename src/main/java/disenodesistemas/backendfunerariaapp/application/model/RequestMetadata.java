package disenodesistemas.backendfunerariaapp.application.model;

public record RequestMetadata(
    String ipAddress, String userAgent, String deviceIdHeader, String idempotencyKey) {}
