package disenodesistemas.backendfunerariaapp.cucumberjunit.bdd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
public class CucumberSpringConfiguration {
  @Autowired protected TestRestTemplate testRestTemplate;
}
