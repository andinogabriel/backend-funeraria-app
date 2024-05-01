package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.DeceasedTestDataFactory.getDeceasedRequestDto;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import disenodesistemas.backendfunerariaapp.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.DeceasedResponseDto;
import disenodesistemas.backendfunerariaapp.entities.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.repository.DeceasedRepository;
import disenodesistemas.backendfunerariaapp.service.converters.AbstractConverter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

@ExtendWith(MockitoExtension.class)
class DeceasedServiceImplTest {

  @Mock private DeceasedRepository deceasedRepository;
  @Mock private ProjectionFactory projectionFactory;
  @Mock private ModelMapper mapper;
  @Mock private AbstractConverter<DeceasedEntity, DeceasedRequestDto> converter;
  @InjectMocks private DeceasedServiceImpl sut;

  private DeceasedResponseDto deceasedResponseDto;

  @BeforeEach
  void setUp() {
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    deceasedResponseDto =
        projectionFactory.createProjection(DeceasedResponseDto.class, getDeceasedRequestDto());
  }

  @Test
  void findAll() {
    final List<DeceasedResponseDto> expectedResult = List.of(deceasedResponseDto);
    given(deceasedRepository.findAllByOrderByRegisterDateDesc()).willReturn(expectedResult);

    final List<DeceasedResponseDto> actualResponse = sut.findAll();

    assertAll(
        () -> assertEquals(expectedResult.size(), actualResponse.size()),
        () -> assertEquals(expectedResult.get(0).getDni(), actualResponse.get(0).getDni()));
  }

  @Test
  void create() {}

  @Test
  void update() {}

  @Test
  void delete() {}

  @Test
  void findByDni() {}
}
