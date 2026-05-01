package disenodesistemas.backendfunerariaapp.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Spring's caching abstraction backed by Caffeine and registers a single
 * {@link CacheManager} for the whole application. Catalog query use cases use this manager to
 * memoize responses for read-heavy, near-immutable lookups (brand, category, gender, province,
 * relationship, receipt-type, death-cause, role, city), while the matching command use cases
 * evict the corresponding cache when data is mutated.
 *
 * <p>The Caffeine specification is intentionally conservative: a 10-minute write expiry keeps
 * cached entries fresh enough that operational changes are visible quickly, while a 500-entry
 * cap protects memory even if the catalog set grows. Cache names are created on demand the first
 * time a use case references them, so adding a new catalog cache only requires updating the
 * relevant {@link org.springframework.cache.annotation.Cacheable} annotation, not this class.
 */
@Configuration
@EnableCaching
public class CacheConfig {

  /** Cache namespace prefix shared by every catalog-level cache name. */
  public static final String CATALOG_PREFIX = "catalog.";

  public static final String BRAND_CACHE = CATALOG_PREFIX + "brand";
  public static final String CATEGORY_CACHE = CATALOG_PREFIX + "category";
  public static final String DEATH_CAUSE_CACHE = CATALOG_PREFIX + "deathCause";
  public static final String GENDER_CACHE = CATALOG_PREFIX + "gender";
  public static final String PROVINCE_CACHE = CATALOG_PREFIX + "province";
  public static final String CITY_BY_ID_CACHE = CATALOG_PREFIX + "city.byId";
  public static final String CITY_BY_PROVINCE_CACHE = CATALOG_PREFIX + "city.byProvince";
  public static final String RECEIPT_TYPE_CACHE = CATALOG_PREFIX + "receiptType";
  public static final String RELATIONSHIP_CACHE = CATALOG_PREFIX + "relationship";
  public static final String ROLE_CACHE = CATALOG_PREFIX + "role";

  /**
   * Builds the Caffeine-backed cache manager used by every {@code @Cacheable} annotation in the
   * application. The manager creates caches lazily so any newly introduced cache name on a use
   * case immediately gets the shared specification without additional wiring.
   */
  @Bean
  public CacheManager cacheManager() {
    final CaffeineCacheManager cacheManager = new CaffeineCacheManager();
    cacheManager.setCaffeine(
        Caffeine.newBuilder().maximumSize(500).expireAfterWrite(Duration.ofMinutes(10)));
    return cacheManager;
  }
}
