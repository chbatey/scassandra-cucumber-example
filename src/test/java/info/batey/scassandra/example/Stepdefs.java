package info.batey.scassandra.example;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.collect.ImmutableMap;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.scassandra.Scassandra;
import org.scassandra.ScassandraFactory;
import org.scassandra.http.client.PrimingRequest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Stepdefs {

    private final static Scassandra SCASSANDRA = ScassandraFactory.createServer();

    private WireMockServer wireMock = new WireMockServer();

    private UserService underTest;
    private UserInformation userInformation;
    private UnableToRetrieveUserInformationException exceptionThrown;

    private String id = "chbatey";

    @After
    public void after() {
        this.wireMock.stop();
        if (underTest != null) {
            this.underTest.shutdown();
        }
        SCASSANDRA.stop();
    }

    @Before
    public void beforeScenario() {
        SCASSANDRA.start();
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

    @Then("^Unable to retrieve user information is returned$")
    public void verifyExceptionThrown() throws Throwable {
        assertTrue("Expected exception", this.exceptionThrown != null);
    }

    @Given("^The user had been retrieved previously$")
    public void The_user_had_been_retrieved_previously() throws Throwable {
        PrimingRequest existingUser = PrimingRequest.queryBuilder()
                .withQuery("select firstName, lastName from users where id = 'chbatey'")
                .withRows(
                        ImmutableMap.of("firstName", "Chris",
                                "lastName", "Batey"))
                .build();
        SCASSANDRA.primingClient().primeQuery(existingUser);
    }

    @Given("^The data store is down$")
    public void cassandraDown() throws Throwable {
        SCASSANDRA.stop();
    }

    @Given("^The data store has problems writing$")
    public void cassandraWriteTimeouts() throws Throwable {
        PrimingRequest existingUser = PrimingRequest.queryBuilder()
                .withQuery("insert into users(id, firstName, lastName) values ('chbatey','Chris','Batey')")
                .withResult(PrimingRequest.Result.write_request_timeout)
                .build();
        SCASSANDRA.primingClient().primeQuery(existingUser);
    }
}
