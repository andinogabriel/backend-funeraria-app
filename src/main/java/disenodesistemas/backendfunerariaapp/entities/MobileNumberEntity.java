package disenodesistemas.backendfunerariaapp.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"mobileNumbers", "handler","hibernateLazyInitializer"}, allowSetters = true)
    @JoinColumn(name = "supplier_id")
    private SupplierEntity supplierNumber;

    public MobileNumberEntity(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(!(obj instanceof MobileNumberEntity)) return false;
        MobileNumberEntity a = (MobileNumberEntity) obj;

        return this.getMobileNumber() != null && this.getMobileNumber().equals(a.getMobileNumber());
    }


}
