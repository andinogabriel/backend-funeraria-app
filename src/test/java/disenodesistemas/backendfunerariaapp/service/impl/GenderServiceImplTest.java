package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.GenderTestDataFactory.getEntityFemaleGender;
import static disenodesistemas.backendfunerariaapp.utils.GenderTestDataFactory.getFemaleGender;
import static disenodesistemas.backendfunerariaapp.utils.GenderTestDataFactory.getMaleGender;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.response.GenderResponseDto;
import disenodesistemas.backendfunerariaapp.entities.GenderEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.GenderRepository;
import java.util.List;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
class GenderServiceImplTest {

  @Mock private GenderRepository genderRepository;
  @InjectMocks private GenderServiceImpl sut;

  private ProjectionFactory projectionFactory;

  @BeforeEach
  void setUp() {
    projectionFactory = new SpelAwareProxyProjectionFactory();
  }

  @Test
  void getGenders() {
    final List<GenderResponseDto> expectedResult =
        Stream.of(getFemaleGender(), getMaleGender())
            .map(gender -> projectionFactory.createProjection(GenderResponseDto.class, gender))
            .collect(Collectors.toUnmodifiableList());
    given(genderRepository.findAllProjectedBy()).willReturn(expectedResult);

    final List<GenderResponseDto> actualResult = sut.getGenders();

    assertAll(
        () -> assertFalse(actualResult.isEmpty()),
        () ->
            assertTrue(
                actualResult.stream()
                    .anyMatch(gender -> "masculino".equalsIgnoreCase(gender.getName()))),
        () ->
            assertTrue(
                actualResult.stream()
                    .anyMatch(gender -> "femenino".equalsIgnoreCase(gender.getName()))));
    then(genderRepository).should(times(1)).findAllProjectedBy();
  }

  @Test
  void getGenderById() {
    final GenderEntity expectedResult = getEntityFemaleGender();
    final Long id = expectedResult.getId();
    given(genderRepository.findById(id)).willReturn(Optional.of(expectedResult));

    final GenderEntity actualResult = sut.getGenderById(id);

    assertAll(
        () -> assertEquals(expectedResult.getId(), actualResult.getId()),
        () -> assertEquals(expectedResult.getName(), actualResult.getName()));
    then(genderRepository).should(times(1)).findById(id);
  }

  @Test
  void getGenderByIdThrowsNotFoundException() {
    final Long NON_EXISTING_ID = 123456L;
    given(genderRepository.findById(NON_EXISTING_ID))
        .willThrow(new NotFoundException("gender.error.not.found"));

    final NotFoundException actualResult =
        assertThrows(NotFoundException.class, () -> sut.getGenderById(NON_EXISTING_ID));

    assertAll(
        () -> assertEquals(HttpStatus.NOT_FOUND, actualResult.getStatus()),
        () -> assertEquals("gender.error.not.found", actualResult.getMessage()));
    then(genderRepository).should(times(1)).findById(NON_EXISTING_ID);
  }
}
