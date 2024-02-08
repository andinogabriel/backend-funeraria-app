package disenodesistemas.backendfunerariaapp.cucumberjunit.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("disenodesistemas/backendfunerariaapp/cucumberjunit/bdd")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "disenodesistemas.backendfunerariaapp.cucumberjunit.bdd")
public class CucumberTest {
}