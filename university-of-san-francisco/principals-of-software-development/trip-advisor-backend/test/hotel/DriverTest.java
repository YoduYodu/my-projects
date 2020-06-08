package hotel;

import database.Driver;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DriverTest {
    private Driver driver;

    @Before
    public void setUp() throws Exception {
        driver = new Driver();
    }

    @Test
    public void getHotel() {
        assertTrue(driver.getHotel());
    }

    @Test
    public void getReviews() {
        assertTrue(driver.getReviews());
    }
}