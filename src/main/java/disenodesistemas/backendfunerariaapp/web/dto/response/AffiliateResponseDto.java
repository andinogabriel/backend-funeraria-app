package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import disenodesistemas.backendfunerariaapp.web.dto.UserDto;
import java.time.LocalDate;
import java.util.List;

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
    List<AddressResponseDto> addresses
) {}