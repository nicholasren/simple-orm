package mvc.functional;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class PostTest extends FunctionalTest {


    @Test
    public void should_accept_post_request() {
        driver.get(JETTY_SERVER_URL + "/house/new");

        WebElement name = driver.findElement(By.name("house.name"));
        WebElement doorHeight = driver.findElement(By.name("house.door.height"));
        WebElement doorWidth = driver.findElement(By.name("house.door.width"));

        name.sendKeys("Doudou");
        doorHeight.sendKeys("2");
        doorWidth.sendKeys("1");

        name.submit();

        assertThat(getBody(), equalTo("house: name: Doudou, door: width: 1m, height: 2m"));
    }
}