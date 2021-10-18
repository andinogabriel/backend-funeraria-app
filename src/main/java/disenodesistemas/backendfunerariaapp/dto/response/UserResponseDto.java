package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import disenodesistemas.backendfunerariaapp.enums.RoleName;

import java.util.Date;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface UserResponseDto {

    long getId();
    String getFirstName();
    String getLastName();
    String getEmail();
    Date getStartDate();
    boolean getEnabled();
    List<MobileNumberResponseDto> getMobileNumbers();
    List<AddressResponseDto> getAddresses();
    Set<RoleEntity> getRoles();

    interface RoleEntity {
        long getId();
        RoleName getName();
    }
}
