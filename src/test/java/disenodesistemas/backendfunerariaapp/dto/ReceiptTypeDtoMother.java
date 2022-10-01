package disenodesistemas.backendfunerariaapp.dto;

import lombok.experimental.UtilityClass;


@UtilityClass
public class ReceiptTypeDtoMother {

    private static final String RECIBO_DE_CAJA = "Recibo de caja";

    public static ReceiptTypeDto getReciboDeCaja() {
        return ReceiptTypeDto
                .builder()
                .id(1L)
                .name(RECIBO_DE_CAJA)
                .build();
    }

}