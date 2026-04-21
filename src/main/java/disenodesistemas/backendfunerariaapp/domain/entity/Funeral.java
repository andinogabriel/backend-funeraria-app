package disenodesistemas.backendfunerariaapp.domain.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "funeral")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Funeral implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @Column(nullable = false)
  private LocalDateTime funeralDate;

  @Column(nullable = false, length = 50, unique = true)
  private String receiptNumber;

  @Column(nullable = false, length = 50)
  private String receiptSeries;

  @Digits(integer = 3, fraction = 2)
  private BigDecimal tax;

  @Column(nullable = false)
  @Digits(integer = 9, fraction = 2)
  private BigDecimal totalAmount;

  private LocalDateTime registerDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receipt_type_id")
  private ReceiptTypeEntity receiptType;

  // @MapsId
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "deceased_id", referencedColumnName = "id")
  private DeceasedEntity deceased;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "plan_id")
  private Plan plan;

  @Builder
  public Funeral(
      final LocalDateTime funeralDate,
      final String receiptNumber,
      final String receiptSeries,
      final BigDecimal tax,
      final BigDecimal totalAmount,
      final ReceiptTypeEntity receiptType,
      final DeceasedEntity deceased,
      final Plan plan) {
    this.funeralDate = funeralDate;
    this.receiptNumber = receiptNumber;
    this.receiptSeries = receiptSeries;
    this.tax = tax;
    this.totalAmount = totalAmount;
    this.registerDate = LocalDateTime.now();
    this.receiptType = receiptType;
    this.deceased = deceased;
    this.plan = plan;
  }
}
