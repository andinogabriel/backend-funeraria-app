package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import disenodesistemas.backendfunerariaapp.web.dto.UserDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DeceasedResponseDto(
    Long id,
    String firstName,
    String lastName,
    Integer dni,
    boolean affiliated,
    @JsonFormat(pattern = "dd-MM-yyyy") LocalDate birthDate,
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm") LocalDateTime registerDate,
    @JsonFormat(pattern = "dd-MM-yyyy") LocalDate deathDate,
    AddressResponseDto placeOfDeath,
    RelationshipResponseDto deceasedRelationship,
    UserDto deceasedUser,
    GenderResponseDto gender,
    DeathCauseResponseDto deathCause,
    /** Coverage verdict recorded at registration (ADR-0022); absent when not an affiliate. */
    Boolean coverageEligible,
    /** Reason codes when not covered: MISSING_BILLING_DATA, WAITING_PERIOD, ACCOUNT_IN_ARREARS. */
    List<String> coverageReasons
) {}