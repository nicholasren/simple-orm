package mvc.functional;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GetTest extends FunctionalTest {

    @Test
    public void should_response_get_request() {
        driver.get(JETTY_SERVER_URL + "/house/index");
        assertThat(getBody(), is("this is the house index page, there are 0 houses"));
    }

    @Test
    public void should_response_show_of_get_request() {
        driver.get(JETTY_SERVER_URL + "/house/show?id=1");
        assertThat(getBody(), is("this is the house index page, there are 0 houses"));
    }
}

