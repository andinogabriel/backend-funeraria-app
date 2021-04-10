package disenodesistemas.backendfunerariaapp.dto;

import disenodesistemas.backendfunerariaapp.entities.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.entities.ServiceDetailEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter @Setter
public class ServiceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String serviceId;
    private Date serviceDate;
    private String receiptNumber;
    private String receiptSeries;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private Date registerDate;
    private ReceiptTypeDto receiptType;
    private DeceasedEntity deceased;
    private List<ServiceDto> serviceDetails = new ArrayList<>();

}
