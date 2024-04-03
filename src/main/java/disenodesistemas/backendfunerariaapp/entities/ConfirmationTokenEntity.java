package disenodesistemas.backendfunerariaapp.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.time.Instant;

@Entity(name = "confirmation_tokens")
@Getter
@Setter
@NoArgsConstructor
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
