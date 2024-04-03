package disenodesistemas.backendfunerariaapp.cucumberjunit.bdd.steps;

import disenodesistemas.backendfunerariaapp.cucumberjunit.bdd.CucumberSpringConfiguration;
import disenodesistemas.backendfunerariaapp.dto.DeviceInfo;
import disenodesistemas.backendfunerariaapp.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserLoginDto;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.Assertions;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginSteps extends CucumberSpringConfiguration {

    private String username;
    private ResponseEntity<JwtDto> response;

    @Given("admin user wants to login")
    public void adminUserWantsToLogin() {
        username = "admin@gmail.com";
    }

    @When("the user tries to login as admin")
    public void theUserTriesToLoginAsAdmin() {
        response = testRestTemplate.postForEntity(
                "/api/v1/users/login", UserLoginDto.builder()
                        .email(username)
                        .password("asd123asd")
                        .deviceInfo(DeviceInfo.builder()
                                .deviceId("aaaa-aaaa-aaaa-aaaa")
                                .deviceType("BROWSER_CHROME")
                                .build())
                        .build(),
                JwtDto.class);
    }

    @Then("the user has admin role.")
    public void theUserIsAllowedToUseTheApp() {
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).getAuthorities().contains("ROLE_ADMIN"));
    }
}
