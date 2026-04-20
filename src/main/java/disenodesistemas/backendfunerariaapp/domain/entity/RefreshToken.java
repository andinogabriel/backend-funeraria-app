package disenodesistemas.backendfunerariaapp.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;
import java.time.Instant;

@Entity(name = "refresh_tokens")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RefreshToken {

  @Id
  @Column(nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String tokenHash;

  @OneToOne(optional = false, cascade = CascadeType.ALL)
  @JoinColumn(name = "user_device_id", unique = true)
  private UserDevice userDevice;

  private Long refreshCount;

  @Column(nullable = false)
  private Instant expiryDate;

  @Column(nullable = false)
  private Instant issuedAt;

  private Instant lastUsedAt;

  private Instant revokedAt;

  @Version private Long version;
}
