package disenodesistemas.backendfunerariaapp.modern.application.usecase.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.BrandPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.GenderPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.brand.BrandCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.brand.BrandQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.gender.GenderQueryUseCase;
import disenodesistemas.backendfunerariaapp.config.CacheConfig;
import disenodesistemas.backendfunerariaapp.domain.entity.BrandEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.GenderEntity;
import disenodesistemas.backendfunerariaapp.mapping.BrandMapper;
import disenodesistemas.backendfunerariaapp.mapping.GenderMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.GenderResponseDto;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Verifies that catalog query use cases memoize their results through Spring's caching
 * abstraction and that command use cases evict the corresponding cache when the catalog data
 * changes. The test uses a real Spring context (so the {@code @Cacheable} / {@code @CacheEvict}
 * AOP proxies actually wrap the beans) with mocked persistence ports.
 */
@SpringJUnitConfig(CatalogCacheBehaviorTest.TestConfig.class)
@DisplayName("Catalog cache behavior")
class CatalogCacheBehaviorTest {

  @Autowired private BrandQueryUseCase brandQueryUseCase;
  @Autowired private BrandCommandUseCase brandCommandUseCase;
  @Autowired private GenderQueryUseCase genderQueryUseCase;
  @Autowired private BrandPersistencePort brandPersistencePort;
  @Autowired private BrandMapper brandMapper;
  @Autowired private GenderPersistencePort genderPersistencePort;
  @Autowired private GenderMapper genderMapper;
  @Autowired private CacheManager cacheManager;

  @BeforeEach
  void clearCachesAndResetMocks() {
    cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    reset(brandPersistencePort, brandMapper, genderPersistencePort, genderMapper);
  }

  @Test
  @DisplayName(
      "Given a catalog with cached findAll when the same query is invoked twice then the persistence port is called only once")
  void givenACatalogWithCachedFindAllWhenTheSameQueryIsInvokedTwiceThenThePersistencePortIsCalledOnlyOnce() {
    final BrandEntity brand = new BrandEntity();
    final BrandResponseDto dto = new BrandResponseDto(1L, "Acme", null);
    when(brandPersistencePort.findAllByOrderByName()).thenReturn(List.of(brand));
    when(brandMapper.toDto(brand)).thenReturn(dto);

    assertThat(brandQueryUseCase.findAll()).containsExactly(dto);
    assertThat(brandQueryUseCase.findAll()).containsExactly(dto);

    verify(brandPersistencePort, times(1)).findAllByOrderByName();
  }

  @Test
  @DisplayName(
      "Given a cached findById when the same id is requested twice then the persistence port resolves it only once")
  void givenACachedFindByIdWhenTheSameIdIsRequestedTwiceThenThePersistencePortResolvesItOnlyOnce() {
    final BrandEntity brand = new BrandEntity();
    final BrandResponseDto dto = new BrandResponseDto(7L, "Cached", null);
    when(brandPersistencePort.findById(7L)).thenReturn(Optional.of(brand));
    when(brandMapper.toDto(brand)).thenReturn(dto);

    assertThat(brandQueryUseCase.findById(7L)).isEqualTo(dto);
    assertThat(brandQueryUseCase.findById(7L)).isEqualTo(dto);

    verify(brandPersistencePort, times(1)).findById(7L);
  }

  @Test
  @DisplayName(
      "Given findById is cached per id when distinct ids are requested then the persistence port resolves each id once")
  void givenFindByIdIsCachedPerIdWhenDistinctIdsAreRequestedThenThePersistencePortResolvesEachIdOnce() {
    final BrandEntity brand1 = new BrandEntity();
    final BrandEntity brand2 = new BrandEntity();
    final BrandResponseDto dto1 = new BrandResponseDto(1L, "One", null);
    final BrandResponseDto dto2 = new BrandResponseDto(2L, "Two", null);
    when(brandPersistencePort.findById(1L)).thenReturn(Optional.of(brand1));
    when(brandPersistencePort.findById(2L)).thenReturn(Optional.of(brand2));
    when(brandMapper.toDto(brand1)).thenReturn(dto1);
    when(brandMapper.toDto(brand2)).thenReturn(dto2);

    brandQueryUseCase.findById(1L);
    brandQueryUseCase.findById(2L);
    brandQueryUseCase.findById(1L);
    brandQueryUseCase.findById(2L);

    verify(brandPersistencePort, times(1)).findById(1L);
    verify(brandPersistencePort, times(1)).findById(2L);
  }

  @Test
  @DisplayName(
      "Given a cached findAll when create is invoked then the cache is evicted and the next findAll re-reads the persistence port")
  void givenACachedFindAllWhenCreateIsInvokedThenTheCacheIsEvictedAndTheNextFindAllReReadsThePersistencePort() {
    final BrandEntity brand = new BrandEntity();
    final BrandResponseDto dto = new BrandResponseDto(1L, "Acme", null);
    when(brandPersistencePort.findAllByOrderByName()).thenReturn(List.of(brand));
    when(brandMapper.toDto(brand)).thenReturn(dto);
    when(brandMapper.toEntity(any(BrandRequestDto.class))).thenReturn(brand);
    when(brandPersistencePort.save(brand)).thenReturn(brand);

    brandQueryUseCase.findAll();
    brandCommandUseCase.create(BrandRequestDto.builder().name("Newco").build());
    brandQueryUseCase.findAll();

    verify(brandPersistencePort, times(2)).findAllByOrderByName();
  }

  @Test
  @DisplayName(
      "Given a cached findAll when update is invoked then the next findAll re-reads the persistence port")
  void givenACachedFindAllWhenUpdateIsInvokedThenTheNextFindAllReReadsThePersistencePort() {
    final BrandEntity brand = new BrandEntity();
    final BrandResponseDto dto = new BrandResponseDto(1L, "Acme", null);
    when(brandPersistencePort.findAllByOrderByName()).thenReturn(List.of(brand));
    when(brandPersistencePort.findById(1L)).thenReturn(Optional.of(brand));
    when(brandMapper.toDto(brand)).thenReturn(dto);
    when(brandPersistencePort.save(brand)).thenReturn(brand);

    brandQueryUseCase.findAll();
    brandCommandUseCase.update(1L, BrandRequestDto.builder().name("Renamed").build());
    brandQueryUseCase.findAll();

    verify(brandPersistencePort, times(2)).findAllByOrderByName();
  }

  @Test
  @DisplayName(
      "Given a read-only catalog with cached findAll when the same query is invoked twice then the persistence port is called only once")
  void givenAReadOnlyCatalogWithCachedFindAllWhenTheSameQueryIsInvokedTwiceThenThePersistencePortIsCalledOnlyOnce() {
    final GenderEntity gender = new GenderEntity();
    final GenderResponseDto dto = new GenderResponseDto(1L, "Female");
    when(genderPersistencePort.findAllByOrderByName()).thenReturn(List.of(gender));
    when(genderMapper.toDto(gender)).thenReturn(dto);

    assertThat(genderQueryUseCase.getGenders()).containsExactly(dto);
    assertThat(genderQueryUseCase.getGenders()).containsExactly(dto);

    verify(genderPersistencePort, times(1)).findAllByOrderByName();
  }

  @Configuration
  @Import(CacheConfig.class)
  static class TestConfig {

    @Bean
    BrandPersistencePort brandPersistencePort() {
      return mock(BrandPersistencePort.class);
    }

    @Bean
    BrandMapper brandMapper() {
      return mock(BrandMapper.class);
    }

    @Bean
    GenderPersistencePort genderPersistencePort() {
      return mock(GenderPersistencePort.class);
    }

    @Bean
    GenderMapper genderMapper() {
      return mock(GenderMapper.class);
    }

    @Bean
    BrandQueryUseCase brandQueryUseCase(
        final BrandPersistencePort brandPersistencePort, final BrandMapper brandMapper) {
      return new BrandQueryUseCase(brandPersistencePort, brandMapper);
    }

    @Bean
    BrandCommandUseCase brandCommandUseCase(
        final BrandPersistencePort brandPersistencePort,
        final BrandMapper brandMapper,
        final BrandQueryUseCase brandQueryUseCase) {
      return new BrandCommandUseCase(brandPersistencePort, brandMapper, brandQueryUseCase);
    }

    @Bean
    GenderQueryUseCase genderQueryUseCase(
        final GenderPersistencePort genderPersistencePort, final GenderMapper genderMapper) {
      return new GenderQueryUseCase(genderPersistencePort, genderMapper);
    }
  }
}
