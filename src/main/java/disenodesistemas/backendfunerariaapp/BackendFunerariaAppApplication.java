package disenodesistemas.backendfunerariaapp;

import disenodesistemas.backendfunerariaapp.dto.UserDto;
import disenodesistemas.backendfunerariaapp.models.responses.UserRest;
import disenodesistemas.backendfunerariaapp.security.AppProperties;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class BackendFunerariaAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendFunerariaAppApplication.class, args);
	}

	//Creamos una sola instancia de esta clase para poder utilizarlas en todas partes
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}


	@Bean
	public SpringApplicationContext springApplicationContext() {
		return new SpringApplicationContext();
	}

	@Bean(name = "AppProperties")
	public AppProperties getAppProperties() {
		return new AppProperties();
	}

	//Para tener una instancia ModelMapper global para no estar instanciando cada rato
	@Bean
	public ModelMapper modelMapper() {
		ModelMapper mapper = new ModelMapper();

		//Cuando mapee de UserDto a la clase UserRest no queremos que traigan los afiliados de UserRest
		mapper.typeMap(UserDto.class, UserRest.class).addMappings(m -> m.skip(UserRest::setAffiliates));
		//Cuando se este mapeando de AffiliateDto a AffiliateRest, evitamos que la columna de affiliates de UserRest se mapee para evitar mapeo infinito

		return mapper;
	}

}
