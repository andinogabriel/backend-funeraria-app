package disenodesistemas.backendfunerariaapp.dto.response;

public interface CityResponseDto {

    long getId();
    String getName();
    String getZipCode();
    ProvinceEntity getProvince();

    interface ProvinceEntity {
        long getId();
        String getName();
    }

}
