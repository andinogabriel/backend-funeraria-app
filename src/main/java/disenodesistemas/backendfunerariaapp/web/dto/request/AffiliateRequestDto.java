package disenodesistemas.backendfunerariaapp.web.dto.request;

import disenodesistemas.backendfunerariaapp.web.dto.GenderDto;
import disenodesistemas.backendfunerariaapp.web.dto.RelationshipDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
public record AffiliateRequestDto(
    Long id,
    @NotBlank(message = "{affiliate.error.empty.firstName}") String firstName,
    @NotBlank(message = "{affiliate.error.empty.lastName}") String lastName,
    @NotNull(message = "{affiliate.error.null.birthDate}") @Past(message = "{affiliate.error.invalid.birthDate}") LocalDate birthDate,
    @NotNull(message = "{affiliate.error.null.dni}") Integer dni,
    @NotNull(message = "{affiliate.error.null.relationship}") RelationshipDto relationship,
    @NotNull(message = "{affiliate.error.null.gender}") GenderDto gender,
    /** Who enrols, pays and claims for this affiliate. */
    @NotNull(message = "{affiliate.error.holder.empty}") @Valid MembershipHolderRequestDto holder,
    /** Health tier code from the tariff (STANDARD, GRADED, SPECIAL...): prices the fee. */
    @NotBlank(message = "{affiliate.error.healthTier.empty}") String healthTierCode,
    /**
     * Enrolment date ("fecha de alta"); drives the claim waiting period. Optional: today on
     * create, unchanged on update when omitted. {@code @PastOrPresent} checks against the JVM
     * clock (UTC in production), which is ahead of the business zone: lenient, never stricter.
     */
    @PastOrPresent(message = "{affiliate.error.enrolmentDate.future}") LocalDate enrolmentDate) {}
