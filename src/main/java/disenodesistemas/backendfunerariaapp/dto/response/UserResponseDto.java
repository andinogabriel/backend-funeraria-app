package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import disenodesistemas.backendfunerariaapp.enums.Role;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface UserResponseDto {

    long getId();
    String getFirstName();
    String getLastName();
    String getEmail();
    Boolean getActive();

    @JsonFormat(pattern="dd-MM-yyyy")
    LocalDate getStartDate();

    boolean getEnabled();
    List<MobileNumberResponseDto> getMobileNumbers();
    List<AddressResponseDto> getAddresses();
    Set<RoleEntity> getRoles();

    interface RoleEntity {
        long getId();
        Role getName();
    }
}
