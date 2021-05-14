package disenodesistemas.backendfunerariaapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class ReceiptTypeDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private List<EntryDto> entries = new ArrayList<>();
    private List<ServiceDto> services = new ArrayList<>();
}
