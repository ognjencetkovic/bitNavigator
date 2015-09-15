import org.junit.*;

import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import play.Logger;
import play.Play;
import play.mvc.*;
import play.test.*;
import play.data.DynamicForm;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.RequiredValidator;
import play.i18n.Lang;
import play.libs.F;
import play.libs.F.*;
import play.twirl.api.Content;

import static org.junit.Assert.assertNull;
import static play.test.Helpers.*;
import static org.junit.Assert.*;

public class IntegrationTest {

    @Before
    public void setUp() {
        fakeApplication(inMemoryDatabase());
    }

    @Test
    public void test() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())),
                HTMLUNIT, new Callback<TestBrowser>() {
                    public void invoke(TestBrowser browser) {
                        browser.goTo("http://localhost:3333");
                        assertTrue(browser.pageSource().contains("bitNavigator"));
                    }
                });
    }

    @Test
    public void testRegistration() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())),
                HTMLUNIT, new Callback<TestBrowser>() {
                    public void invoke(TestBrowser browser) {
                        browser.goTo("http://localhost:3333/service/add");
                        browser.fill("#inputDefault").with("Something");
                        browser.submit("#btn-add-service");
                    }
                });
    }

}
