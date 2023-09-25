package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface BrandResponseDto {
    Long getId();
    String getName();
    String getWebPage();
}
