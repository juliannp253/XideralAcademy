package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SantanderHomePage;

public class BancaPrivadaTest extends BaseTest {

    @Test
    public void testRedireccionBancaPrivada() {
        SantanderHomePage homePage = new SantanderHomePage(driver);

        homePage.clickBancaPrivada();

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/bp/home/"),
                "Redirección fallida. URL actual: " + currentUrl);

        driver.get("https://www.santander.com.mx/");
    }
}