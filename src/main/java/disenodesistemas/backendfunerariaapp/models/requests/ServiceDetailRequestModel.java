package disenodesistemas.backendfunerariaapp.models.requests;

import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.entities.ServiceEntity;

import java.math.BigDecimal;

public class ServiceDetailRequestModel {

    private Integer quantity;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private ItemEntity itemService;
    private ServiceEntity service;

}
