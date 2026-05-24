package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import disenodesistemas.backendfunerariaapp.web.dto.UserDto;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Response payload for an affiliate. {@code deletedAt} and {@code deletedBy} are populated
 * only by the admin-only papelera endpoint; the regular listing surfaces never see them
 * because every read filters {@code deletedAt is null}. {@code JsonInclude.NON_NULL} keeps
 * the two fields out of the wire shape for active affiliates so the existing listing
 * payload stays untouched byte-for-byte.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AffiliateResponseDto(
    String firstName,
    String lastName,
    Integer dni,
    @JsonFormat(pattern = "dd-MM-yyyy") LocalDate birthDate,
    @JsonFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
    Boolean deceased,
    GenderResponseDto gender,
    RelationshipResponseDto relationship,
    UserDto user,
    List<MobileNumberResponseDto> mobileNumbers,
    List<AddressResponseDto> addresses,
    /** UTC instant the affiliate was soft-deleted. Null for active affiliates. */
    Instant deletedAt,
    /** Email of the admin that requested the soft-delete. Null for active affiliates. */
    String deletedBy
) {}
