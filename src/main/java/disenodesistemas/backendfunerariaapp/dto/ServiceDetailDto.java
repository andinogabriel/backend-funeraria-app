package disenodesistemas.backendfunerariaapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
public class ServiceDetailDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String serviceDetailId;
    private Integer quantity;
    private ServiceDto service;
    private ItemDto item;

}
