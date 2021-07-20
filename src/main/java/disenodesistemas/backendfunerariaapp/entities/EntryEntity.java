package disenodesistemas.backendfunerariaapp.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(name = "entries")
@SQLDelete(sql = "UPDATE entries SET deleted = true WHERE id=?")
@FilterDef(name = "deletedEntriesFilter", parameters = @ParamDef(name = "isDeleted", type = "boolean")) //Define los requerimientos, los cuales, serán usados por @Filter
@Filter(name = "deletedEntriesFilter", condition = "deleted = :isDeleted") //Condición para aplicar el filtro en función del parámetro
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
public class EntryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 20)
    private Integer receiptNumber;

    @Column(nullable = false, length = 10)
    private Integer receiptSeries;

    @CreatedDate
    private Date entryDate;

    @Digits(integer = 2, fraction = 2)
    private BigDecimal tax;

    @Column(columnDefinition = "numeric(8,2) default 0")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "receipt_type_id")
    private ReceiptTypeEntity receiptType;

    @ManyToOne
    @JsonIgnoreProperties(value = {"entries", "handler","hibernateLazyInitializer"}, allowSetters = true)
    @JoinColumn(name = "supplier_id")
    private SupplierEntity entrySupplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"entries", "handler","hibernateLazyInitializer"}, allowSetters = true)
    @JoinColumn(name = "user_id")
    private UserEntity entryUser;

    private boolean deleted = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_modified_id")
    private UserEntity lastModifiedBy;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entry")
    @JsonManagedReference
    private List<EntryDetailEntity> entryDetails = new ArrayList<>();


}
