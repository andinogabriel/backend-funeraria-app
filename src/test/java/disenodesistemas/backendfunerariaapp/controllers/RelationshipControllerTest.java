package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.utils.RelationshipTestDataFactory.getGrandMotherRelationshipEntity;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.response.RelationshipResponseDto;
import disenodesistemas.backendfunerariaapp.service.RelationshipService;
import java.util.List;
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
class RelationshipControllerTest {

  @Mock private RelationshipService relationshipService;
  @InjectMocks private RelationshipController sut;
  private RelationshipResponseDto relationshipResponseDto;

  @BeforeEach
  void setUp() {
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    relationshipResponseDto =
        projectionFactory.createProjection(
            RelationshipResponseDto.class, getGrandMotherRelationshipEntity());
  }

  @Test
  void findAll() {
    final List<RelationshipResponseDto> expectedList = List.of(relationshipResponseDto);
    given(relationshipService.getRelationships()).willReturn(expectedList);
    final ResponseEntity<List<RelationshipResponseDto>> actualResult = sut.findAll();
    assertAll(
        () -> assertEquals(HttpStatus.OK, actualResult.getStatusCode()),
        () -> assertEquals(expectedList, actualResult.getBody()));
    then(relationshipService).should(times(1)).getRelationships();
  }
}
