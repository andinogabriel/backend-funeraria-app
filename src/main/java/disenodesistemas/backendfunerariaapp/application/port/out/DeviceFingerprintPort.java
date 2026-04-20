package disenodesistemas.backendfunerariaapp.application.port.out;

public interface DeviceFingerprintPort {

  String fingerprint(String deviceId, String userAgent);
}
