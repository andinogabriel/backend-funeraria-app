package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface CityResponseDto {

    Long getId();
    String getName();
    String getZipCode();
    ProvinceEntity getProvince();

    interface ProvinceEntity {
        Long getId();
        String getName();
    }

}
