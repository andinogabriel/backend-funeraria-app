package disenodesistemas.backendfunerariaapp.entities;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SupplierEntityMother {

    private static final String NAME = "Proveedorazo";
    private static final String NIF = "NIF123ASD";
    private static final String EMAIL = "proveedor@gmail.com";

    public static SupplierEntity getSupplier() {
        final SupplierEntity supplier = new SupplierEntity(NAME, NIF, null, EMAIL);
        supplier.setId(1L);
        return supplier;
    }

}