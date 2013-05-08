package mvc.functional;

import com.thoughtworks.mvc.main.JettyLauncher;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class FunctionalTest {
    public static final String JETTY_SERVER_URL = "http://localhost:8080/sample";
    protected WebDriver driver;

    @Before
    public void setUp() {
        JettyLauncher.start("sample/src/main/webapp", "/sample");
        driver = new ChromeDriver();
    }

    @After
    public void tearDown() {
        driver.quit();
        try {
            JettyLauncher.stop();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    protected String getBody() {
        return driver.findElement(By.tagName("body")).getText();
    }
}
