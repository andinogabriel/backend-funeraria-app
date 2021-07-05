package disenodesistemas.backendfunerariaapp.dto.response;

import disenodesistemas.backendfunerariaapp.entities.RoleEntity;
import disenodesistemas.backendfunerariaapp.enums.RoleName;

import java.util.Date;
import java.util.Set;

public interface UserResponseDto {

    long getId();
    String getFirstName();
    String getLastName();
    String getEmail();
    Date getStartDate();
    boolean getEnabled();
    MobileNumberResponseDto getMobileNumbers();
    AddressResponseDto getAddresses();
    Set<RoleEntity> getRoles();

    interface RoleEntity {
        long getId();
        RoleName getName();
    }
}
