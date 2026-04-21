package disenodesistemas.backendfunerariaapp.modern.support;

public final class TestValues {

  public static final String USER_EMAIL = "john.doe@example.com";
  public static final String MISSING_USER_EMAIL = "missing@example.com";
  public static final String USER_FIRST_NAME = "John";
  public static final String USER_LAST_NAME = "Doe";
  public static final String ENCODED_PASSWORD = "encoded-password";

  public static final String DEVICE_ID = "device-123";
  public static final String ALTERNATE_DEVICE_ID = "device-999";
  public static final String DEVICE_TYPE = "mobile";
  public static final String IP_ADDRESS = "127.0.0.1";
  public static final String USER_AGENT = "JUnit-Test-Agent";
  public static final String IDEMPOTENCY_KEY = "idem-123";
  public static final String MISSING_REFRESH_TOKEN = "missing-refresh-token";

  public static final String JWT_SECRET =
      "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
  public static final String JWT_AUTHORITIES_CLAIM = "authorities";
  public static final String JWT_TOKEN_PREFIX = "Bearer";
  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String DEVICE_ID_CLAIM = "device_id";
  public static final String DEVICE_FINGERPRINT_CLAIM = "device_fingerprint";
  public static final String DEVICE_VERSION_CLAIM = "device_version";
  public static final String DEVICE_ID_HEADER = "X-Device-Id";
  public static final String IDEMPOTENCY_HEADER = "Idempotency-Key";
  public static final String SECURITY_REQUEST_SECRET = "test-secret";

  public static final String BRAND_NAME = "Acme";
  public static final String BRAND_WEB_PAGE = "https://acme.example";
  public static final String CATEGORY_NAME = "Urnas";
  public static final String CATEGORY_DESCRIPTION = "Productos funerarios";
  public static final String ITEM_NAME = "Urna";
  public static final String ITEM_DESCRIPTION = "Urna premium";
  public static final String ITEM_CODE = "ITEM-001";
  public static final String SUPPLIER_NIF = "20-12345678-9";
  public static final String SUPPLIER_NAME = "Proveedor Uno";
  public static final String SUPPLIER_WEB_PAGE = "https://supplier.example";
  public static final String SUPPLIER_EMAIL = "proveedor@example.com";
  public static final String FUNERAL_RECEIPT_NUMBER = "REC-123";
  public static final String FUNERAL_RECEIPT_SERIES = "SER-001";

  private TestValues() {}
}
