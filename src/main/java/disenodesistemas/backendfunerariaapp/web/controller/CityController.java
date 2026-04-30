package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.city.CityQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.response.CityResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/cities")
public class CityController {

  private final CityQueryUseCase cityQueryUseCase;

  @GetMapping(path = "/{id}")
  public ResponseEntity<CityResponseDto> findById(@PathVariable final Long id) {
    return ResponseEntity.ok(cityQueryUseCase.findById(id));
  }

  @GetMapping
  public ResponseEntity<List<CityResponseDto>> findByProvinceId(
      @RequestParam(value = "province_id") final Long id) {
    return ResponseEntity.ok(cityQueryUseCase.findByProvinceId(id));
  }
}
