package disenodesistemas.backendfunerariaapp.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity(name = "confirmation_tokens")
@Getter @Setter
@NoArgsConstructor
public class ConfirmationTokenEntity implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 100)
    private String token;

    private Timestamp expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"confirmationTokens", "handler","hibernateLazyInitializer"}, allowSetters = true)
    @JoinColumn(nullable = false, name = "user_id")
    private UserEntity user;

    public ConfirmationTokenEntity(UserEntity user, String token) {
        this.user = user;
        this.token = token;
    }
}
