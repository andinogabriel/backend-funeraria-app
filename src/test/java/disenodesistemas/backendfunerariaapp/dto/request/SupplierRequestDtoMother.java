package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class SupplierRequestDtoMother {

  private static final String NAME = "Proveedorazo";
  private static final String NIF = "NIF123ASD";
  private static final String EMAIL = "proveedor@gmail.com";

  public static SupplierRequestDto getSupplier() {
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
