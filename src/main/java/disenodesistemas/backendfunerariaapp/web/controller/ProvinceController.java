package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.province.ProvinceQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.response.ProvinceResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/provinces")
public class ProvinceController {

  private final ProvinceQueryUseCase provinceQueryUseCase;

  @GetMapping
  public ResponseEntity<List<ProvinceResponseDto>> findAll() {
    return ResponseEntity.ok(provinceQueryUseCase.getAllProvinces());
  }
}
