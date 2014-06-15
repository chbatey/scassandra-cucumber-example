package info.batey.scassandra.example;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Stepdefs {

    private WireMockServer wireMock = new WireMockServer();

    private UserService underTest;
    private UserInformation userInformation;
    private UnableToRetrieveUserInformationException exceptionThrown;

    private String id = "chbatey";

    @After
    public void after() {
        this.wireMock.stop();
    }

    @Before
    public void beforeScenario() {
        this.wireMock.start();
        this.underTest = new UserService();
        this.userInformation = null;
        this.exceptionThrown = null;
    }

    @Given("^The company wide user repository is available$")
    public void setupUserRepository() throws Throwable {
        givenThat(get(urlEqualTo("/user/" + id)).willReturn(
                aResponse().withBody("{\"firstName\":\"Chris\",\"lastName\":\"Batey\"}")
        ));
    }

    @When("User information is requested")
    public void requestUserInformation() {
        try {
            userInformation = this.underTest.retrieveUserInformation(id);
        } catch (UnableToRetrieveUserInformationException e) {
            this.exceptionThrown = e;
        }
    }

    @Then("^The user information is returned$")
    public void correctUserInformationReturned() throws Throwable {
        assertNotNull("Expected a valid user information to be returned.", this.userInformation);
        assertEquals("Chris", this.userInformation.getFirstName());
        assertEquals("Batey", this.userInformation.getLastName());
    }

    @Given("^The company wide user repository is unavailable$")
    public void userServiceDown() throws Throwable {
        givenThat(get(urlEqualTo("/user/" + id)).willReturn(
                aResponse().withStatus(500)
        ));
    }

    @Then("^Unable to retrieve user information us returned$")
    public void verifyExceptionThrown() throws Throwable {
        assertTrue("Expected exception", this.exceptionThrown != null);
    }

    @Given("^The user had been retrieved previously$")
    public void The_user_had_been_retrieved_previously() throws Throwable {
        givenThat(get(urlEqualTo("/user/" + id)).willReturn(
                aResponse().withBody("{\"firstName\":\"Chris\",\"lastName\":\"Batey\"}")
        ));
        this.underTest.retrieveUserInformation(id);
    }
}
