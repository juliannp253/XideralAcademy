package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.SantanderHomePage;

public class EmpresasMenuTest extends BaseTest {

    public record MenuTestData(By subOption, String expectedUrlFragment) {}

    @DataProvider(name = "empresasData")
    public Object[][] getEmpresasData() {
        return new Object[][] {
                { new MenuTestData(SantanderHomePage.Empresas.LINK_EMPRESAS_GOBIERNO, "/bei/home.html") },
                { new MenuTestData(SantanderHomePage.Empresas.LINK_MULTINACIONALES, "multinacionales") }
        };
    }

    @Test(dataProvider = "empresasData", priority = 1)
    public void testNavegacionMenuEmpresas(MenuTestData testData) {
        SantanderHomePage homePage = new SantanderHomePage(driver);

        homePage.selectMenuOption(SantanderHomePage.MENU_EMPRESAS, testData.subOption());

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains(testData.expectedUrlFragment()),
                "Redirección fallida en Empresas. URL actual: " + currentUrl);

        driver.get("https://www.santander.com.mx/");
    }
}