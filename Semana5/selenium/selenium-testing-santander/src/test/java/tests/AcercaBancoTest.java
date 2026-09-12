package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.SantanderHomePage;

public class AcercaBancoTest extends BaseTest {
    public record MenuTestData(By subOption, String expectedUrlFragment) {}

    @DataProvider(name = "acercaData")
    public Object[][] getAcercaData() {
        return new Object[][] {
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.AcercaBanco.LINK_FUNDACION_SANTANDER, "fundacion-santander.html") },
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.AcercaBanco.LINK_BLOG, "blog.html") },
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.AcercaBanco.LINK_SOSTENIBILIDAD, "acerca-del-banco/responsabilidad-social.html") },
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.AcercaBanco.LINK_EDUCACION_FINANCIERA, "educacion-financiera") },
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.AcercaBanco.LINK_INVERSIONISTAS, "ir/home") },
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.AcercaBanco.LINK_SALA_COMUNICACION, "ceb/sala_prensa_2026.html") },
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.AcercaBanco.LINK_BOLSA_TRABAJO, "personas/bolsa-de-trabajo.html") }
        };
    }

    @Test(dataProvider = "acercaData", priority = 1)
    public void testNavegacionMenuAcercaDeBanco(EmpresasMenuTest.MenuTestData testData) {
        SantanderHomePage homePage = new SantanderHomePage(driver);

        homePage.selectMenuOption(SantanderHomePage.MENU_ACERCA_BANCO, testData.subOption());

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains(testData.expectedUrlFragment()),
                "Redirección fallida en Empresas. URL actual: " + currentUrl);

        driver.get("https://www.santander.com.mx/");
    }
}
