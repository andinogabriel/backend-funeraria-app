package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.web.dto.response.ProvinceResponseDto;
import disenodesistemas.backendfunerariaapp.application.service.ProvinceService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/provinces")
public class ProvinceController {

  private final ProvinceService provinceService;

  public ProvinceController(final ProvinceService provinceService) {
    this.provinceService = provinceService;
  }

  @GetMapping
  public ResponseEntity<List<ProvinceResponseDto>> findAll() {
    return ResponseEntity.ok(provinceService.getAllProvinces());
  }
}
