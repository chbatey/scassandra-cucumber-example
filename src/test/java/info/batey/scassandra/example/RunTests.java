package info.batey.scassandra.example;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@Cucumber.Options(strict=false, format={"pretty", "html:target/cucumber"})
public class RunTests {
}
