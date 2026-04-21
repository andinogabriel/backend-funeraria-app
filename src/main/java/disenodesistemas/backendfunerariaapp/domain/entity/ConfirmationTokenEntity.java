package disenodesistemas.backendfunerariaapp.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "confirmation_tokens")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ConfirmationTokenEntity implements Serializable {

  @Id @GeneratedValue private Long id;

  @Column(nullable = false, length = 100)
  private String token;

  private Instant expiryDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnoreProperties(
      value = {"confirmationTokens", "handler", "hibernateLazyInitializer"},
      allowSetters = true)
  @JoinColumn(nullable = false, name = "user_id")
  private UserEntity user;

  public ConfirmationTokenEntity(final UserEntity user, final String token) {
    this.user = user;
    this.token = token;
  }
}
