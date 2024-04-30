package disenodesistemas.backendfunerariaapp.utils;

import disenodesistemas.backendfunerariaapp.dto.request.SupplierRequestDto;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class SupplierTestDataFactory {

  private static final String NAME = "Proveedorazo";
  private static final String NIF = "NIF123ASD";
  private static final String EMAIL = "proveedor@gmail.com";

  public static SupplierEntity getSupplierEntity() {
    final SupplierEntity supplier = new SupplierEntity(NAME, NIF, null, EMAIL);
    supplier.setId(1L);
    return supplier;
  }

  public static SupplierRequestDto getSupplierRequestDto() {
    return SupplierRequestDto.builder()
        .name(NAME)
        .id(1L)
        .addresses(List.of())
        .email(EMAIL)
        .nif(NIF)
        .webPage(null)
        .build();
  }
}
