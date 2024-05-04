package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.RelationshipTestDataFactory.getGrandMotherRelationshipEntity;
import static disenodesistemas.backendfunerariaapp.utils.RelationshipTestDataFactory.getRelationshipEntities;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.response.RelationshipResponseDto;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.RelationshipRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
class RelationshipServiceImplTest {

  @Mock private RelationshipRepository relationshipRepository;
  @InjectMocks private RelationshipServiceImpl sut;
  private RelationshipEntity relationshipEntity;
  private RelationshipResponseDto relationshipResponseDto;
  private ProjectionFactory projectionFactory;

  @BeforeEach
  void setUp() {
    projectionFactory = new SpelAwareProxyProjectionFactory();
    relationshipEntity = getGrandMotherRelationshipEntity();
    relationshipResponseDto =
        projectionFactory.createProjection(RelationshipResponseDto.class, relationshipEntity);
  }

  @Test
  void getRelationships() {
    final List<RelationshipResponseDto> expectedResult =
        getRelationshipEntities().stream()
            .map(
                relationshipEnt ->
                    projectionFactory.createProjection(
                        RelationshipResponseDto.class, relationshipEnt))
            .collect(Collectors.toUnmodifiableList());
    given(relationshipRepository.findAllByOrderByName()).willReturn(expectedResult);

    final List<RelationshipResponseDto> actualResult = sut.getRelationships();

    assertAll(
        () -> assertFalse(actualResult.isEmpty()), () -> assertSame(expectedResult, actualResult));
    then(relationshipRepository).should(times(1)).findAllByOrderByName();
  }

  @Test
  void getRelationshipById() {
    final RelationshipEntity expectedResult = relationshipEntity;
    final Long id = expectedResult.getId();
    given(relationshipRepository.findById(id)).willReturn(Optional.of(expectedResult));

    final RelationshipEntity actualResult = sut.getRelationshipById(id);

    assertSame(expectedResult, actualResult);
    then(relationshipRepository).should(times(1)).findById(id);
  }

  @Test
  void getRelationshipByIdThrowsNotFoundException() {
    final Long NON_EXISTING_ID = 321L;
    given(relationshipRepository.findById(NON_EXISTING_ID))
        .willThrow(new NotFoundException("relationship.error.not.found"));

    final NotFoundException actualResult =
        assertThrows(NotFoundException.class, () -> sut.getRelationshipById(NON_EXISTING_ID));

    assertAll(
        () -> assertEquals(HttpStatus.NOT_FOUND, actualResult.getStatus()),
        () -> assertEquals("relationship.error.not.found", actualResult.getMessage()));
    then(relationshipRepository).should(times(1)).findById(NON_EXISTING_ID);
  }
}
