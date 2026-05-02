package disenodesistemas.backendfunerariaapp.utils;

/**
 * Standard response shape returned by API endpoints whose business outcome is a state
 * transition (delete/logout/etc.) rather than a domain object. The 'name' identifies the
 * operation (typically the all-caps verb plus aggregate, e.g. {@code "DELETE BRAND"}) and
 * 'result' carries the outcome label ({@code "SUCCESSFUL"} today; reserved for richer values
 * such as {@code "PARTIAL"} or {@code "QUEUED"} when a future endpoint needs them).
 */
public record OperationStatusModel(String name, String result) {}
