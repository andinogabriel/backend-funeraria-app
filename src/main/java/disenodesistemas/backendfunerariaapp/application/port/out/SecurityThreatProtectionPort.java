package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.application.model.RequestMetadata;

public interface SecurityThreatProtectionPort {

  void assertLoginAllowed(String email, String deviceId, RequestMetadata requestMetadata);

  void recordLoginFailure(String email, String deviceId, RequestMetadata requestMetadata);

  void recordLoginSuccess(String email, String deviceId, RequestMetadata requestMetadata);

  void assertRequestAllowed(String principal, String deviceId, RequestMetadata requestMetadata);

  void recordSuspiciousRequest(
      String principal, String deviceId, RequestMetadata requestMetadata, String reason);
}
