package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import disenodesistemas.backendfunerariaapp.web.dto.UserDto;
import java.math.BigDecimal;
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
    String deletedBy,
    /** Holder (titular) who pays for this affiliate. Null for pre-billing legacy rows. */
    MembershipHolderResponseDto holder,
    /** Health tier the fee was priced with. Null for legacy rows. */
    HealthTierDto healthTier,
    /** Label of the age band the fee was priced with (e.g. "36-50"). Null for legacy rows. */
    String ageBandLabel,
    /** Monthly fee assigned at enrolment / last repricing. Null for legacy rows. */
    BigDecimal monthlyFee,
    @JsonFormat(pattern = "dd-MM-yyyy") LocalDate feeCalculatedOn,
    /** Enrolment date ("fecha de alta") the claim waiting period counts from. */
    @JsonFormat(pattern = "dd-MM-yyyy") LocalDate enrolmentDate
) {}
