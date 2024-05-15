package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.service.CityService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/cities")
public class CityController {

  private final CityService cityService;

  public CityController(final CityService cityService) {
    this.cityService = cityService;
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity<CityResponseDto> findById(@PathVariable final Long id) {
    return ResponseEntity.ok(cityService.findById(id));
  }

  @GetMapping
  public ResponseEntity<List<CityResponseDto>> findByProvinceId(
      @RequestParam(value = "province_id") final Long id) {
    return ResponseEntity.ok(cityService.findByProvinceId(id));
  }
}
