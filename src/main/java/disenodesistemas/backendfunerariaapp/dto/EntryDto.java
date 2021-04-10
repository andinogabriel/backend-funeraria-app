package disenodesistemas.backendfunerariaapp.dto;

import disenodesistemas.backendfunerariaapp.entities.EntryDetailEntity;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
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
public class EntryDto implements Serializable {

    private static final long serialVersionUID = 1L;


    private long id;
    private String entryId;
    private Integer receiptNumber;
    private Integer receiptSeries;
    private Date entryDate;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private ReceiptTypeEntity receiptType;
    private SupplierDto entrySupplier;
    private UserEntity entryUser;
    private List<EntryDetailEntity> entryDetails = new ArrayList<>();

}
