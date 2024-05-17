package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.utils.GenderTestDataFactory.getEntityFemaleGender;
import static disenodesistemas.backendfunerariaapp.utils.GenderTestDataFactory.getEntityMaleGender;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import disenodesistemas.backendfunerariaapp.dto.response.GenderResponseDto;
import disenodesistemas.backendfunerariaapp.service.GenderService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class GenderControllerTest {

  @Mock private GenderService genderService;
  @InjectMocks private GenderController sut;

  private ProjectionFactory projectionFactory;

  @BeforeEach
  void setUp() {
    projectionFactory = new SpelAwareProxyProjectionFactory();
  }

  @Test
  void findAll() {
    final List<GenderResponseDto> genders =
        Stream.of(getEntityFemaleGender(), getEntityMaleGender())
            .map(
                genderEntity ->
                    projectionFactory.createProjection(GenderResponseDto.class, genderEntity))
            .collect(Collectors.toUnmodifiableList());
    given(genderService.getGenders()).willReturn(genders);

    final ResponseEntity<List<GenderResponseDto>> actualResult = sut.findAll();

    assertAll(
        () -> assertEquals(HttpStatus.OK, actualResult.getStatusCode()),
        () -> assertEquals(genders, actualResult.getBody()));
  }
}
