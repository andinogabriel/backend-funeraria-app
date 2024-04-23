package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public class FuneralMother {

  public FuneralRequestDto getFuneralRequestDto() {
    return FuneralRequestDto.builder()
        .funeralDate(LocalDateTime.parse("2023-11-13 03:00:00"))
        .build();
  }
}
