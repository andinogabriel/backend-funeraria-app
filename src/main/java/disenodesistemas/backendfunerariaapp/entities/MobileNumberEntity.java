package disenodesistemas.backendfunerariaapp.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Entity(name = "mobileNumbers")
@Getter @Setter
@NoArgsConstructor
public class MobileNumberEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String mobileNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity userNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"mobileNumbers", "handler","hibernateLazyInitializer"}, allowSetters = true)
    @JoinColumn(name = "supplier_id")
    private SupplierEntity supplierNumber;

    public MobileNumberEntity(final String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @Override
    public boolean equals(final Object obj) {
        if(this == obj) return true;
        if(!(obj instanceof MobileNumberEntity)) return false;
        val a = (MobileNumberEntity) obj;
        return this.getMobileNumber() != null && this.getMobileNumber().equals(a.getMobileNumber());
    }

}
