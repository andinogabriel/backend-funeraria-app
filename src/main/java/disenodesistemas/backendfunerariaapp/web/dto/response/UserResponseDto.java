package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import disenodesistemas.backendfunerariaapp.web.dto.RolesDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponseDto(
    Long id,
    String firstName,
    String lastName,
    String email,
    Boolean active,
    @JsonFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
    boolean enabled,
    List<MobileNumberResponseDto> mobileNumbers,
    List<AddressResponseDto> addresses,
    Set<RolesDto> roles
) {}