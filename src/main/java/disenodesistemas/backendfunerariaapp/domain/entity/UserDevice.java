package disenodesistemas.backendfunerariaapp.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "user_devices")
public class UserDevice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  private String deviceType;

  @Column(nullable = false)
  private String deviceId;

  @Column(nullable = false, length = 64)
  private String fingerprintHash;

  @OneToOne(optional = false, mappedBy = "userDevice")
  private RefreshToken refreshToken;

  private Boolean isRefreshActive;

  private Long tokenVersion;

  private Instant lastSeenAt;

  private String lastIpAddress;

  @Version private Long version;

  public void identifyDevice(final String deviceId, final String deviceType) {
    this.deviceId = deviceId;
    this.deviceType = deviceType;
  }

  public void registerSession(
      final UserEntity userEntity,
      final String deviceId,
      final String deviceType,
      final String fingerprintHash,
      final String ipAddress,
      final Long tokenVersion,
      final Instant lastSeenAt) {
    this.user = userEntity;
    identifyDevice(deviceId, deviceType);
    activateRefresh(fingerprintHash, ipAddress, tokenVersion, lastSeenAt);
  }

  public void activateRefresh(
      final String fingerprintHash,
      final String ipAddress,
      final Long tokenVersion,
      final Instant lastSeenAt) {
    this.fingerprintHash = fingerprintHash;
    this.isRefreshActive = Boolean.TRUE;
    this.tokenVersion = tokenVersion;
    this.lastSeenAt = lastSeenAt;
    this.lastIpAddress = ipAddress;
  }

  public void deactivateRefresh(final Long tokenVersion, final Instant lastSeenAt) {
    this.isRefreshActive = Boolean.FALSE;
    this.tokenVersion = tokenVersion;
    this.lastSeenAt = lastSeenAt;
  }

  public boolean hasDeviceId(final String candidateDeviceId) {
    return Objects.equals(deviceId, candidateDeviceId);
  }

  public boolean hasFingerprint(final String candidateFingerprint) {
    return Objects.equals(fingerprintHash, candidateFingerprint);
  }
}
